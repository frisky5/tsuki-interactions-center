package interactions_center.agents_manager.webservices.administration.users;

import interactions_center.agents_manager.json.requests.Agent;
import interactions_center.agents_manager.json.responses.GenericResponse;
import interactions_center.config_manager.CONFIG_STORE_KEYS;
import interactions_center.config_manager.ConfigStore;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Objects;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestPath;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("/v1/agents_manager/admin/users/agent/{agentId}")
@NoCache
public class UpdateAgent {

  private final Logger LOG = LoggerFactory.getLogger(UpdateAgent.class);

  @Inject
  PgPool pgPool;
  @Inject
  Keycloak keycloak;
  @Inject
  ConfigStore configStore;

  @POST
  @Blocking
  @RolesAllowed({"admin:agent:edit", "supervisor:agent:edit"})
  public Response update(@RestPath String agentId, Agent agent) {

    GenericResponse response = new GenericResponse();
    UserResource user;

    try {
      user =
          keycloak.realm(configStore.get(CONFIG_STORE_KEYS.KEYCLOAK_REALM_NAME)).users()
              .get(agentId);
    } catch (Exception e) {
      LOG.error("failed to get user with id [{}] from keycloak",agentId, e);
      response.setError(true);
      response.setMessage(
          "failed to get user from keycloak, make sure the user id is correct, if not check the logs");
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }

    if(Objects.isNull(user)){
      LOG.error("return user resource of id [{}] from keycloak is null",agentId);
      response.setError(true);
      response.setMessage(
          "failed to get user from keycloak, returned user resource is null, make sure the user id is correct, if not check the logs");
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }

    if(user.groups().stream().noneMatch(groupRepresentation -> groupRepresentation.getName().equals(configStore.get(CONFIG_STORE_KEYS.KEYCLOAK_AGENTS_GROUP_NAME)))){
      LOG.error("user resource with id [{}] doesn't belong to agents group, thus won't be updated",agentId);
      response.setError(true);
      response.setMessage(
          "user your are trying to update doesn't belong to agents group");
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }

    response.setError(false);
    return Response.ok(response).build();
  }
}
