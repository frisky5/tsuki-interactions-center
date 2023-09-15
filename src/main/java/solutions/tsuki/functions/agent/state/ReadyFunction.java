package solutions.tsuki.functions.agent.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.utils.ExecutorsFactory;
import solutions.tsuki.ic.agents.constants.AGENT_STATES;
import solutions.tsuki.functions.database.InsertAgentStateEventFunction;
import solutions.tsuki.ic.queues.manager.functions.queuesOfAgents.SortedInsertAgentIntoAssignedQueuesFunction;
import solutions.tsuki.ic.queues.manager.functions.queueOfQueuesOfInteractions.ProcessFunction;
import solutions.tsuki.ic.queues.manager.functions.queueOfQueuesOfInteractions.ValidateByAgentFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;

import java.util.Arrays;
import java.util.function.Function;

@ApplicationScoped
public class ReadyFunction implements Function<AgentRequest, Integer> {

    public final Logger LOG = LoggerFactory.getLogger("LogoutAgent-Function");

    @Inject
    QueuesStores queuesStores;

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
        Agent agent = queuesStores.getAgentsStore().get(request.getId());
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
        LOG.info("agent [{}] state changed to ready, idle at time is [{}]", agent.getKeycloakUserUuid(),
                agent.getTimeMeasurements().getIdleAt());
        return 0;
    }
}
