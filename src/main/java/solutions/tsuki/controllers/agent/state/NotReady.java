package solutions.tsuki.controllers.agent.state;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.NoCache;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.functions.agent.state.NotReadyFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.stores.StoresDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
@Path("/v1/agent/state/not_ready")
@NoCache
public class NotReady {
    @Inject
    SecurityIdentity identity;
    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    StoresDTO storesDTO;

    @Inject
    ExecutorsFactory executorsFactory;

    @Inject
    NotReadyFunction notReadyFunction;


    @POST
    public Uni<AgentResponse> run() {

        return Uni.createFrom().item(jsonWebToken.getSubject()).onItem().transform(userId -> {
                    Agent agent = storesDTO.getAgentsStore().get(userId);
                    AgentResponse response = new AgentResponse();
                    if (agent == null) {
                        response.setError(true);
                        response.setMessage("agent not found");
                        return response;
                    }

                    try {
                        agent.lock();
                        AgentRequest agentRequest = new AgentRequest();
                        agentRequest.setId(userId);
                        agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));

                        int result = notReadyFunction.apply(agentRequest);
                        if (result != 0) {
                            response.setError(true);
                            response.setCode(result);
                            return response;
                        }


                        response.setError(false);
                        response.setState(AGENT_STATES.NOT_READY);
                        return response;
                    } finally {
                        agent.unlock();
                    }
                })
                .runSubscriptionOn(executorsFactory.getMultiThreadExecutor());
    }
}
