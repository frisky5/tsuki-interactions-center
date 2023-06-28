package solutions.tsuki.controllers.agent;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import solutions.tsuki.configuration.SingleThreadExecutor;
import solutions.tsuki.functions.agent.GoReadyFunction;
import solutions.tsuki.functions.agent.LoginAgentFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
@Path("/v1/agent/state/ready")
public class GoReadyAgentController {

    @Inject
    public SingleThreadExecutor singleThreadExecutor;

    @Inject
    public GoReadyFunction goReadyFunction;

    @POST
    public Uni<AgentResponse> login(AgentRequest agentRequest) {
        agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));
        return Uni.createFrom().item(agentRequest).onItem().transform(goReadyFunction)
                .runSubscriptionOn(singleThreadExecutor.getExecutor());
    }
}
