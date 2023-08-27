package solutions.tsuki.functions.database;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.constants.sql.AGENT_QUERIES;
import solutions.tsuki.json.requests.AgentRequest;

import java.util.function.Function;

@ApplicationScoped
public class InsertAgentStateEventFunction implements Function<AgentRequest, Integer> {
    public final Logger logger = LoggerFactory.getLogger("InsertAgentStateEvent");
    @Inject
    PgPool pgPool;

    @Override
    public Integer apply(AgentRequest agentRequest) {
        return pgPool.preparedQuery(AGENT_QUERIES.INSERT_AGENT_STATE_EVENT)
                .execute(Tuple.of(agentRequest.getId(), agentRequest.getState(), agentRequest.getRequestAt()))
                .onItemOrFailure().transform((item, throwable) -> {
                    if (throwable != null) {
                        logger.error("failed to write agent state event for agent [{}]", agentRequest.getId(),
                                throwable);
                        return -1;
                    } else {
                        return 0;
                    }
                }).await().indefinitely();
    }
}
