package solutions.tsuki.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queues.QueueOfAgents;
import solutions.tsuki.queues.QueueOfInteractions;
import solutions.tsuki.queues.QueueOfQueuesOfInteractions;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class QueuesOfAgentsStore {
    @Inject
    QueueOfQueuesOfInteractions queueOfQueuesOfInteractions;

    public final Logger logger = LoggerFactory.getLogger("QueuesOfAgentsStore");

    public final ConcurrentHashMap<Integer, QueueOfAgents> store = new ConcurrentHashMap<>(20);

    public void put(QueueOfAgents queue) {
        store.put(queue.getId(), queue);
    }

    public QueueOfAgents get(Integer id) {
        return store.get(id);
    }

    public QueueOfAgents get(QueueOfInteractions queueOfInteractions) {
        return store.get(queueOfInteractions.getId());
    }

    public Agent getHead(Integer queueId) {
        return store.get(queueId) != null ? store.get(queueId).getHead() : null;
    }

    public int sizeOfQueue(Integer id) {
        return store.get(id) != null ? store.get(id).size() : -1;
    }

    public void sortedInsert(Agent agent) {
        agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(assignedQueue -> {
            int insertedAt = store.get(assignedQueue).sortedInsert(agent);
            logger.info("agent [{}] is inserted in queue of agents [{}] at index [{}]", agent.getId(), assignedQueue, insertedAt);
        });
    }

    public void removeAgent(Agent agent) {
        agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(assignedQueue -> {
            store.get(assignedQueue).remove(agent);
            logger.info("agent [{}] is removed from queue of agents [{}]", agent.getId(), assignedQueue);
        });
    }
}
