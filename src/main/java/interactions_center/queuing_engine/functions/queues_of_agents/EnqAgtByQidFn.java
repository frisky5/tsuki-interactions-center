package interactions_center.queuing_engine.functions.queues_of_agents;

import interactions_center.agents_manager.stores.items.Agent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.queues_manager.stores.QueuesOfAgentsStore;
import interactions_center.queues_manager.queues.QueueOfAgents;

import java.util.UUID;
import java.util.function.BiConsumer;

@ApplicationScoped
public class EnqAgtByQidFn implements BiConsumer<Agent, UUID> {

    private final Logger logger = LoggerFactory.getLogger(EnqAgtByQidFn.class);

    @Inject
    QueuesOfAgentsStore queuesOfAgentsStore;

    @Override
    public void accept(Agent agent, UUID queueId) {
        //agent must be already locked here as this function is called from agents_manager module when state is changed
        QueueOfAgents queueOfAgents = queuesOfAgentsStore.get(queueId);
        queueOfAgents.lock();
        logger.info("LOCKED queue of agents [{}]", queueOfAgents.getId());
        try {
            logger.info("LOCKED queue of agents [{}]", queueOfAgents.getId());
            int insertionIndex = queueOfAgents.sortedInsert(agent);
            logger.info("agent [{}] enqueued into queue of agents [{}] at index [{}]",
                    agent.getId(), queueId, insertionIndex);
        } finally {
            queueOfAgents.unlock();
            logger.info("UNLOCKED queue of agents [{}]", queueOfAgents.getId());
        }
    }
}
