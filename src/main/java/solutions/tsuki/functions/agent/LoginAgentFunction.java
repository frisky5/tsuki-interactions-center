package solutions.tsuki.functions.agent;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.configuration.SingleThreadExecutor;
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.constants.sql.AGENT_QUERIES;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queues.QueueOfQueuesOfInteractions;
import solutions.tsuki.stores.AgentsStore;
import solutions.tsuki.stores.QueuesOfAgentsStore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
@ApplicationScoped
public class LoginAgentFunction implements Function<AgentRequest, AgentResponse> {

    public final Logger logger = LoggerFactory.getLogger("LoginAgent-Function");

    @Inject
    public SingleThreadExecutor singleThreadExecutor;
    @Inject
    public QueuesOfAgentsStore queuesOfAgentsStore;
    @Inject
    QueueOfQueuesOfInteractions queueOfQueuesOfInteractions;
    @Inject
    public AgentsStore agentsStore;
    @Inject
    public PgPool pgPool;

    @Override
    public AgentResponse apply(AgentRequest request) {
        AgentResponse response = new AgentResponse();
        Agent agent = agentsStore.get(request.getId());
        if (Objects.isNull(agent)) {
            response.setError(true);
            response.setMessage("agent doesn't exist");
            return response;
        }

        boolean allowed = Arrays.stream(AGENT_STATES.ALLOWED_TO_LOGIN)
                .anyMatch(state -> state == agent.getState());
        if (!allowed) {
            response.setError(true);
            response.setMessage("not allowed to login");
            return response;
        }

        boolean result = pgPool.preparedQuery(AGENT_QUERIES.INSERT_AGENT_STATE_CHANGE_EVENT)
                .execute(Tuple.of(agent.getId(), AGENT_STATES.LOGGED_IN, request.getRequestAt()))
                .onItemOrFailure().transform((item, throwable) -> {
                    if (throwable != null) {
                        logger.error("failed to insert agent [{}] login event into DB", agent.getId(), throwable);
                        return false;
                    } else {
                        return true;
                    }
                }).await().indefinitely();

        if (!result) {
            response.setError(true);
            response.setMessage("failed to change state");
            return response;
        }
        agent.getTimeMeasurements().setLoggedInAt(request.getRequestAt());
        agent.setState(AGENT_STATES.LOGGED_IN);
        logger.info("agent [{}] state changed to logged in, logged in at time is [{}]", agent.getId(),
                agent.getTimeMeasurements().getLoggedInAt());
        response.setError(false);
        return response;

    }
}