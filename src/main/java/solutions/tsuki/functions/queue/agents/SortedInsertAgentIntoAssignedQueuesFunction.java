package solutions.tsuki.functions.queue.agents;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queue.agents.QueueOfAgents;
import solutions.tsuki.stores.StoresDTO;

import java.util.function.Function;

@ApplicationScoped
public class SortedInsertAgentIntoAssignedQueuesFunction implements Function<Agent, Void> {

    public final Logger LOG = LoggerFactory.getLogger("SortedInsertAgentIntoAssignedQueuesFunction");

    @Inject
    StoresDTO storesDTO;

    @Override
    public Void apply(Agent agent) {
        agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(assignedQueue -> {
            QueueOfAgents queueOfAgents = storesDTO.getQueuesOfAgentsStore().get(assignedQueue);
            if (queueOfAgents == null) {
                LOG.warn("queue of interactions of id [{}] was found in assigned queues of agent [{}] but its value " +
                        "in" +
                        " the map is null", assignedQueue, agent.getId());
                return;
            }
            try {
                queueOfAgents.lock();
                LOG.info("queue of agents [{}] locked", queueOfAgents.getId());
                int insertedAt = queueOfAgents.sortedInsert(agent);
                LOG.info("agent [{}] is inserted into queue of agents [{}] at index [{}]", agent.getId(), assignedQueue,
                        insertedAt);
            } finally {
                queueOfAgents.unlock();
                LOG.info("queue of agents [{}] unlocked", queueOfAgents.getId());
            }
        });
        return null;
    }
}
