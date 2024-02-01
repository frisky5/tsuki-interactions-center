package interactions_center.agents_manager.webservices.administration.users;

import interactions_center.config_manager.CONFIG_STORE_KEYS;
import interactions_center.config_manager.ConfigStore;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.agents_manager.json.responses.Agent;
import interactions_center.agents_manager.json.responses.GenericResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Path("/v1/agents_manager/admin/users/agents")
@NoCache
public class GetAgents {
    private final Logger LOG = LoggerFactory.getLogger(GetAgents.class);
    @Inject
    Keycloak keycloak;
    @Inject
    ConfigStore configStore;

    @GET
    @Blocking
    @RolesAllowed({"admin:agent:view", "supervisor:agent:view"})
    public Response get() {
        GenericResponse response = new GenericResponse();
        String realmName = configStore.get(CONFIG_STORE_KEYS.KEYCLOAK_REALM_NAME);
        String agentsGroupName = configStore.get(CONFIG_STORE_KEYS.KEYCLOAK_AGENTS_GROUP_NAME);

        RealmResource icRealm;
        try {
            icRealm = keycloak.realm(realmName);
        } catch (Exception e) {
            LOG.error("failed to get realm with name [{}], agents store will not initialize", realmName, e);
            response.setError(true);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

        GroupResource agentsGroup;
        String agentsGroupId;
        try {
            GroupRepresentation agentsGroupRep =
                    icRealm.groups().groups().stream().filter(groupRepresentation -> groupRepresentation.getName().equals(agentsGroupName)).findFirst().orElse(null);
            if (Objects.isNull(agentsGroupRep)) {
                LOG.error("failed to get agents group with name [{}], agents store will not initialize", agentsGroupName);
                response.setError(true);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
            }
            agentsGroupId = agentsGroupRep.getId();
        } catch (Exception e) {
            LOG.error("failed to get agents group with name [{}], agents store will not initialize", agentsGroupName, e);
            response.setError(true);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

        try {
            agentsGroup = icRealm.groups().group(agentsGroupId);
            if (Objects.isNull(agentsGroup)) {
                LOG.error("failed to get agents group by id [{}], agents store will not initialize",
                        agentsGroupId);
                response.setError(true);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
            }
        } catch (Exception e) {
            LOG.error("failed to get agents group by id [{}], agents store will not initialize",
                    agentsGroupId, e);
            response.setError(true);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

        List<UserRepresentation> users;
        try {
            users = agentsGroup.members();
            if (Objects.isNull(users)) {
                LOG.error("failed to get members (agents) from the agents group either no users are assigned to " +
                        "agents group or keycloak failed to retrieve, agents store" +
                        " will not initialize");
                response.setError(true);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
            }
            if (users.isEmpty()) {
                LOG.warn("agents group contains no users, make sure you add users to it for the agents store to " +
                        "initialize");
                response.setError(true);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
            }
        } catch (Exception e) {
            LOG.error("failed to get members (agents) from the agents group, agents store will not initialize", e);
            response.setError(true);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }


        ArrayList<Agent> agentsRes = new ArrayList<>(10);
        users.forEach(agent -> {
            Agent temp = new Agent();
            temp.setId(agent.getId());
            temp.setUsername(agent.getUsername());
            temp.setFirstName(agent.getFirstName());
            temp.setLastName(agent.getLastName());
            agentsRes.add(temp);
        });
        response.setAgents(agentsRes);
        return Response.ok(response).build();
    }
}
