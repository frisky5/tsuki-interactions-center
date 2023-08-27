package solutions.tsuki.controllers.agent.state;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.stores.AgentsStore;

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
        return Uni.createFrom().item(new AgentResponse()).onItem().transform((agentResponse) -> {
            Agent agent = agentsStore.get(jsonWebToken.getSubject());
            if (agent == null) {
                agentResponse.setError(true);
                agentResponse.setMessage("agent doesn't exist");
                return agentResponse;
            }
            agentResponse.setState(agent.getState());
            return agentResponse;
        });
    }
}
