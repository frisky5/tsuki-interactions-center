package solutions.tsuki.webservices.agent.state;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import solutions.tsuki.ic.queues.manager.utils.ExecutorsFactory;
import solutions.tsuki.ic.agents.constants.AGENT_STATES;
import solutions.tsuki.functions.agent.state.LogoutFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;

import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
@Path("/v1/agent/state/logout")
public class Logout {
    @Inject
    SecurityIdentity identity;
    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    QueuesStores queuesStores;

    @Inject
    ExecutorsFactory executorsFactory;

    @Inject
    public LogoutFunction logoutFunction;

    @POST
    public Uni<AgentResponse> logout() {

        return Uni.createFrom().item(jsonWebToken.getSubject()).onItem().transform(userId -> {
                    AgentResponse response = new AgentResponse();
                    Agent agent = queuesStores.getAgentsStore().get(UUID.fromString(userId));
                    if (agent == null) {
                        response = new AgentResponse();
                        response.setError(true);
                        response.setMessage("user id doesn't exist in agents store");
                        return response;
                    }
                    try {
                        agent.lock();
                        AgentRequest agentRequest = new AgentRequest();
                        agentRequest.setId(UUID.fromString(userId));
                        agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));

                        int result = logoutFunction.apply(agentRequest);
                        if (result != 0) {
                            response.setError(true);
                            response.setCode(result);
                            return response;
                        }

                        response.setError(false);
                        response.setState(AGENT_STATES.LOGGED_OUT);
                        return response;
                    } finally {
                        agent.unlock();
                    }
                })
                .runSubscriptionOn(executorsFactory.getMultiThreadExecutor());
    }
}
