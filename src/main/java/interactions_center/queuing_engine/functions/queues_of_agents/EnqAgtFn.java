package interactions_center.queuing_engine.functions.queues_of_agents;

import interactions_center.agents_manager.stores.items.Agent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.queues_manager.stores.QueuesOfAgentsStore;
import interactions_center.queues_manager.queues.QueueOfAgents;

import java.util.Objects;
import java.util.function.Consumer;

@ApplicationScoped
public class EnqAgtFn implements Consumer<Agent> {

    private final Logger LOG = LoggerFactory.getLogger(EnqAgtFn.class);

    @Inject
    QueuesOfAgentsStore queuesOfAgentsStore;

    @Override
    public void accept(Agent agent) {

        agent.getAssignedInteractionsQueues().keySet().forEach(queueOfInteractionId -> {
            QueueOfAgents queueOfAgents = queuesOfAgentsStore.get(queueOfInteractionId);
            if (Objects.isNull(queueOfAgents)) {
                LOG.warn("Queue of Interactions with ID [{}] doesn't have a corresponding Queue Of Agents in Store " +
                        "QueuesOfAgentsStore", queueOfInteractionId);
                return;
            }
            try {
                queueOfAgents.lock();
                LOG.info("LOCKED queue of agents [{}]", queueOfAgents.getId());
                int insertionIndex = queueOfAgents.sortedInsert(agent);
                LOG.info("agent [{}] enqueued into queue of agents [{}] at index [{}]",
                        agent.getId(), queueOfAgents.getId(), insertionIndex);
            } finally {
                queueOfAgents.unlock();
                LOG.info("UNLOCKED queue of agents [{}]", queueOfAgents.getId());
            }
        });
    }
}
