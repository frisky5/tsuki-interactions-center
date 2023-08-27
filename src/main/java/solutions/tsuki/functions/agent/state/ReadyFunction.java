package solutions.tsuki.functions.agent.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.functions.database.InsertAgentStateEventFunction;
import solutions.tsuki.functions.queue.agents.SortedInsertAgentIntoAssignedQueuesFunction;
import solutions.tsuki.functions.queue.queueOfInteractions.ProcessFunction;
import solutions.tsuki.functions.queue.queueOfInteractions.ValidateByAgentFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.stores.StoresDTO;

import java.util.Arrays;
import java.util.function.Function;

@ApplicationScoped
public class ReadyFunction implements Function<AgentRequest, Integer> {

    public final Logger LOG = LoggerFactory.getLogger("LogoutAgent-Function");

    @Inject
    StoresDTO storesDTO;

    @Inject
    public ExecutorsFactory executorsFactory;

    @Inject
    public InsertAgentStateEventFunction insertAgentStateEventFunction;

    @Inject
    ValidateByAgentFunction validateByAgentFunction;

    @Inject
    ProcessFunction processFunction;

    @Inject
    SortedInsertAgentIntoAssignedQueuesFunction sortedInsertAgentIntoAssignedQueuesFunction;

    @Override
    public Integer apply(AgentRequest request) {
        Agent agent = storesDTO.getAgentsStore().get(request.getId());
        if (agent == null) {
            return -2;
        }

        boolean allowed = Arrays.stream(AGENT_STATES.ALLOWED_TO_READY)
                .anyMatch(state -> state == agent.getState());

        if (!allowed) {
            return -1;
        }


        request.setState(AGENT_STATES.READY);

        sortedInsertAgentIntoAssignedQueuesFunction.apply(agent);

        executorsFactory.getDatabaseMultiThreadExecutor().execute(() -> {
            insertAgentStateEventFunction.apply(request);
        });
        executorsFactory.getQqiSingleThreadExecutor().execute(() -> {
            validateByAgentFunction.apply(agent);
        });
        executorsFactory.getQqiSingleThreadExecutor().execute(() -> {
            processFunction.apply(null);
        });

        agent.getTimeMeasurements().setIdleAt(request.getRequestAt());
        agent.setState(AGENT_STATES.READY);
        LOG.info("agent [{}] state changed to ready, idle at time is [{}]", agent.getId(),
                agent.getTimeMeasurements().getIdleAt());
        return 0;
    }
}
