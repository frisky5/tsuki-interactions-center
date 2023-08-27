package solutions.tsuki.controllers.agent.state;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.functions.agent.state.ReadyFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.stores.StoresDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
@Path("/v1/agent/state/ready")
public class Ready {
    @Inject
    SecurityIdentity identity;
    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    public ExecutorsFactory executorsFactory;
    @Inject
    public StoresDTO storesDTO;
    @Inject
    public ReadyFunction readyFunction;

    @POST
    public Uni<AgentResponse> run() {
        return Uni.createFrom().item(jsonWebToken.getSubject()).onItem().transform(userId -> {
                    Agent agent = storesDTO.getAgentsStore().get(userId);
                    AgentResponse response = new AgentResponse();
                    if (agent == null) {
                        response.setError(true);
                        response.setMessage("user id doesn't exist in agents store");
                        return response;
                    }
                    try {
                        agent.lock();
                        AgentRequest agentRequest = new AgentRequest();
                        agentRequest.setId(userId);
                        agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));

                        int result = readyFunction.apply(agentRequest);
                        if (result != 0) {
                            response.setError(true);
                            response.setCode(result);
                            return response;
                        }

                        response.setError(false);
                        response.setState(AGENT_STATES.READY);
                        return response;
                    } finally {
                        agent.unlock();
                    }
                })
                .runSubscriptionOn(executorsFactory.getMultiThreadExecutor());
    }
}
