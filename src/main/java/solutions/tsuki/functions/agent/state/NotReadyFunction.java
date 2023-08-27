package solutions.tsuki.functions.agent.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.functions.database.InsertAgentStateEventFunction;
import solutions.tsuki.functions.queue.agents.RemoveAgentFromAssignedQueuesFunction;
import solutions.tsuki.functions.queue.queueOfInteractions.ValidateByAgentFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.stores.StoresDTO;

import java.util.Arrays;
import java.util.function.Function;

@ApplicationScoped
public class NotReadyFunction implements Function<AgentRequest, Integer> {
    public final Logger LOG = LoggerFactory.getLogger("NotReadyFunction");

    @Inject
    StoresDTO storesDTO;

    @Inject
    ExecutorsFactory executorsFactory;

    @Inject
    InsertAgentStateEventFunction insertAgentStateEventFunction;

    @Inject
    ValidateByAgentFunction validateByAgentFunction;

    @Inject
    RemoveAgentFromAssignedQueuesFunction removeAgentFromAssignedQueuesFunction;

    @Override
    public Integer apply(AgentRequest request) {
        Agent agent = storesDTO.getAgentsStore().get(request.getId());
        if (agent == null) {
            return -2;
        }
        boolean allowed = Arrays.stream(AGENT_STATES.ALLOWED_TO_NOT_READY)
                .anyMatch(state -> state == agent.getState());
        if (!allowed) {
            return -1;
        }

        agent.getTimeMeasurements().setNotReadyAt(request.getRequestAt());
        agent.setState(AGENT_STATES.NOT_READY);
        request.setState(AGENT_STATES.NOT_READY);

        removeAgentFromAssignedQueuesFunction.apply(agent);

        executorsFactory.getDatabaseMultiThreadExecutor().execute(() -> {
            insertAgentStateEventFunction.apply(request);
        });
        executorsFactory.getQqiSingleThreadExecutor().execute(() -> {
            validateByAgentFunction.apply(agent);
        });
        LOG.info("agent [{}] state changed to not ready, not ready at time is [{}]", agent.getId(),
                agent.getTimeMeasurements().getNotReadyAt());
        return 0;

    }
}
