package solutions.tsuki.controllers.agent;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import solutions.tsuki.configuration.SingleThreadExecutor;
import solutions.tsuki.functions.agent.GoNotReadyFunction;
import solutions.tsuki.functions.agent.LoginAgentFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
@Path("/v1/agent/state/not_ready")
public class GoNotReadyAgentController {

    @Inject
    public SingleThreadExecutor singleThreadExecutor;

    @Inject
    public GoNotReadyFunction goNotReadyFunction;

    @POST
    public Uni<AgentResponse> login(AgentRequest agentRequest) {
        agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));
        return Uni.createFrom().item(agentRequest).onItem().transform(goNotReadyFunction)
                .runSubscriptionOn(singleThreadExecutor.getExecutor());
    }
}
