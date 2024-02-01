package interactions_center.agents_manager.webservices.administration.users;

import interactions_center.agents_manager.json.responses.Agent;
import interactions_center.agents_manager.json.responses.GenericResponse;
import interactions_center.agents_manager.json.responses.Group;
import interactions_center.agents_manager.json.responses.Queue;
import interactions_center.config_manager.CONFIG_STORE_KEYS;
import interactions_center.config_manager.ConfigStore;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestPath;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Path("/v1/agents_manager/admin/users/agent/{agentId}")
@NoCache
public class GetAgent {
    private final Logger LOG = LoggerFactory.getLogger(GetAgent.class);
    private final String SELECT_AGENT_ASSIGNED_QUEUES = "SELECT * FROM map_agents_to_queues_view WHERE agent_id=$1";

    @Inject
    PgPool pgPool;
    @Inject
    Keycloak keycloak;
    @Inject
    ConfigStore configStore;

    @GET
    @Blocking
    @RolesAllowed({"admin:agent:view", "supervisor:agent:view"})
    public Response get(@RestPath String agentId) {
        GenericResponse response = new GenericResponse();

        if (agentId.isBlank()) {
            response.setError(true);
            response.setMessage("missing agent id");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        RealmResource icRealm;
        try {
            icRealm = keycloak.realm(configStore.get(CONFIG_STORE_KEYS.KEYCLOAK_REALM_NAME));
        } catch (Exception e) {
            LOG.error("failed to get realm with name [{}], agents store will not initialize", configStore.get(CONFIG_STORE_KEYS.KEYCLOAK_REALM_NAME), e);
            response.setError(true);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
        UserResource userResource;

        try {
            userResource = icRealm.users().get(agentId);
            if (Objects.isNull(userResource)) {
                response.setError(true);
                response.setMessage("agent id doesn't belong to a user in keycloak");
                return Response.status(Response.Status.NOT_FOUND).entity(response).build();
            }

        } catch (Exception e) {
            LOG.error("failed to get user by agent id [{}]", agentId, e);
            response.setError(true);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

        UserRepresentation userRepresentation = userResource.toRepresentation();
        Agent agent = new Agent();
        agent.setId(userRepresentation.getId());
        agent.setFirstName(userRepresentation.getFirstName());
        agent.setLastName(userRepresentation.getLastName());
        agent.setUsername(userRepresentation.getUsername());
        List<Group> groups = new ArrayList<>();
        userResource.groups().forEach(group -> {
            Group temp = new Group();
            temp.setId(group.getId());
            temp.setName(group.getName());
            groups.add(temp);
        });
        agent.setGroups(groups);

        List<Queue> assignedQueues = new ArrayList<>();
        pgPool.preparedQuery(SELECT_AGENT_ASSIGNED_QUEUES).execute(Tuple.of(agentId)).await().indefinitely().iterator().forEachRemaining(row -> {
            Queue temp = new Queue();
            temp.setId(row.getUUID("queue_id"));
            temp.setName(row.getString("queue_name"));
            temp.setPriority(row.getInteger("priority"));
            assignedQueues.add(temp);
        });
        agent.setAssignedQueues(assignedQueues);
        response.setAgent(agent);
        return Response.ok(response).build();
    }
}
