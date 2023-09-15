package solutions.tsuki.functions.agent.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.utils.ExecutorsFactory;
import solutions.tsuki.ic.agents.constants.AGENT_STATES;
import solutions.tsuki.functions.database.InsertAgentStateEventFunction;
import solutions.tsuki.ic.queues.manager.functions.queuesOfAgents.RemoveAgentFromAssignedQueuesFunction;
import solutions.tsuki.ic.queues.manager.functions.queueOfQueuesOfInteractions.ValidateByAgentFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;

import java.util.Arrays;
import java.util.function.Function;

@ApplicationScoped
public class OfferingInteractionFunction implements Function<AgentRequest, Integer> {
    public final Logger LOG = LoggerFactory.getLogger("OfferingInteractionFunction");

    @Inject
    QueuesStores queuesStores;

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
        Agent agent = queuesStores.getAgentsStore().get(request.getId());
        if (agent == null) {
            return -2;
        }
        boolean allowed = Arrays.stream(AGENT_STATES.ALLOWED_TO_OFFER_INTERACTION).anyMatch(state -> state == agent.getState());
        if (!allowed) {
            return -1;
        }

        agent.getTimeMeasurements().setOfferedAt(request.getRequestAt());
        agent.setState(AGENT_STATES.OFFERING_AN_INTERACTION);
        request.setState(AGENT_STATES.OFFERING_AN_INTERACTION);

        removeAgentFromAssignedQueuesFunction.apply(agent);

        executorsFactory.getDatabaseMultiThreadExecutor().execute(() -> {
            insertAgentStateEventFunction.apply(request);
        });
        executorsFactory.getQqiSingleThreadExecutor().execute(() -> {
            validateByAgentFunction.apply(agent);
        });
        LOG.info("agent [{}] state changed to offering an interaction, offered at time is [{}]", agent.getKeycloakUserUuid(),
                agent.getTimeMeasurements().getOfferedAt());
        return 0;
    }
}
