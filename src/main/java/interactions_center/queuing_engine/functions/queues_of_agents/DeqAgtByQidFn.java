package interactions_center.queuing_engine.functions.queues_of_agents;

import interactions_center.agents_manager.stores.items.Agent;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.queues_manager.stores.QueuesOfAgentsStore;
import interactions_center.queues_manager.queues.QueueOfAgents;

import java.util.UUID;
import java.util.function.BiConsumer;

@ApplicationScoped
public class DeqAgtByQidFn implements BiConsumer<Agent, UUID> {

    private final Logger logger = LoggerFactory.getLogger(DeqAgtByQidFn.class);
    private QueuesOfAgentsStore qas;

    public DeqAgtByQidFn(QueuesOfAgentsStore qas) {
        this.qas = qas;
    }

    @Override
    public void accept(Agent agent, UUID queueId) {
        QueueOfAgents queueOfAgents = qas.get(queueId);
        queueOfAgents.lock();
        logger.info("LOCKED queue of agents [{}]", queueOfAgents.getId());
        try {
            queueOfAgents.remove(agent);
            logger.info("dequeued agent [{}] from queue [{}]", agent.getId(), queueOfAgents.getId());
        } finally {
            queueOfAgents.unlock();
            logger.info("UNLOCKED queue of agents [{}]", queueOfAgents.getId());
        }
    }
}
