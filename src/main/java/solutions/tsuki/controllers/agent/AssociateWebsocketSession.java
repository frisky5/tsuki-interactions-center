package solutions.tsuki.controllers.agent;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.stores.AgentsStore;
import solutions.tsuki.stores.AgentsWebsocketSessionsStore;

@ApplicationScoped
@Path("/v1/agent/ws/associate")
public class AssociateWebsocketSession {
    public final Logger logger = LoggerFactory.getLogger("AssociateWebsocketSession");

    @Inject
    AgentsStore agentsStore;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    AgentsWebsocketSessionsStore agentsWebsocketSessionsStore;

    @Inject
    ExecutorsFactory executorsFactory;

    @POST
    public Uni<AgentResponse> associate(AgentRequest agentRequest) {
        return Uni.createFrom().item(agentRequest).onItem().transform((request) -> {
                    AgentResponse response = new AgentResponse();

                    if (!agentsStore.contains(jsonWebToken.getSubject())) {
                        response.setError(true);
                        response.setMessage("user id associated with token is not found");
                        return response;
                    }
                    if (!agentsWebsocketSessionsStore.sessionExist(request.getWsSessionId())) {
                        response.setError(true);
                        response.setMessage("websocket session id doesn't exist");
                        return response;
                    }

                    agentsWebsocketSessionsStore.putUserToSessionMap(jsonWebToken.getSubject(), request.getWsSessionId());
                    logger.info("session [{}] is now associated with agent [{}]",
                            request.getWsSessionId(), jsonWebToken.getSubject());

                    response.setError(false);
                    return response;
                })
                .runSubscriptionOn(executorsFactory.getMultiThreadExecutor());
    }
}
