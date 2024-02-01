package interactions_center.queuing_engine.functions.queues_of_agents;

import interactions_center.agents_manager.stores.items.Agent;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.queues_manager.stores.QueuesOfAgentsStore;
import interactions_center.queues_manager.queues.QueueOfAgents;

import java.util.function.Consumer;

@ApplicationScoped
public class DqAgtFn implements Consumer<Agent> {

    private final Logger logger = LoggerFactory.getLogger(DqAgtFn.class);

    private final QueuesOfAgentsStore qas;

    public DqAgtFn(QueuesOfAgentsStore qas) {
        this.qas = qas;
    }

    @Override
    public void accept(Agent agent) {
        agent.getAssignedInteractionsQueues().keySet().forEach(assignedQueueOfInteractionsId -> {
            QueueOfAgents queueOfAgents = qas.get(assignedQueueOfInteractionsId);
            queueOfAgents.lock();
            logger.info("LOCKED queue of agents [{}]", queueOfAgents.getId());
            try {
                queueOfAgents.remove(agent);
                logger.info("dequeued agent [{}] from queue [{}]", agent.getId(), queueOfAgents.getId());
            } finally {
                queueOfAgents.unlock();
                logger.info("UNLOCKED queue of agents [{}]", queueOfAgents.getId());
            }
        });
    }
}
