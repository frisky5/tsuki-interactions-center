package solutions.tsuki.webservices.agent.state;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.agents.manager.stores.AgentsStore;

@ApplicationScoped
@Path("/v1/agent/state")
public class CurrentState {

  @Inject
  AgentsStore agentsStore;
  @Inject
  SecurityIdentity identity;
  @Inject
  JsonWebToken jsonWebToken;

  @GET
  public Uni<AgentResponse> login() {
    return Uni.createFrom().item(jsonWebToken.getSubject()).onItem().transform((userId) -> {
      UUID userUuid = UUID.fromString(jsonWebToken.getSubject());
      AgentResponse response = new AgentResponse();
      Agent agent = agentsStore.get(userUuid);
      if (agent == null) {
        response.setError(true);
        response.setMessage("agent doesn't exist");
        return response;
      }
      response.setState(agent.getState());
      return response;
    });
  }
}
