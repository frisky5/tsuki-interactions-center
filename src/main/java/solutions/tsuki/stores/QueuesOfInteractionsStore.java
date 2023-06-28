package solutions.tsuki.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.queues.QueueOfInteractions;
import solutions.tsuki.queues.QueueOfQueuesOfInteractions;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class QueuesOfInteractionsStore {

    @Inject
    QueueOfQueuesOfInteractions queueOfQueuesOfInteractions;

    public final Logger logger = LoggerFactory.getLogger("QueuesOfInteractionsStore");
    public final ConcurrentHashMap<Integer, QueueOfInteractions> store = new ConcurrentHashMap<>(20);

    public void put(QueueOfInteractions queue) {
        store.put(queue.getId(), queue);
    }

    public QueueOfInteractions get(Integer id) {
        return store.get(id);
    }

    public boolean hasQueue(Integer id) {
        return store.containsKey(id);
    }

    public Interaction getHead(Integer queueId) {
        return store.get(queueId) != null ? store.get(queueId).getHead() : null;
    }

    public int sizeOfQueue(Integer id) {
        return store.get(id) != null ? store.get(id).size() : -1;
    }

    public void sortedInsert(Interaction interactionToInsert) {
        int insertedAt = store.get(interactionToInsert.getQueueId()).sortedInsert(interactionToInsert);
        logger.info("interaction [{}] is enqueued in queue [{}] at index [{}]", interactionToInsert.getId(),
                interactionToInsert.getQueueId(), insertedAt);
    }

    public boolean dequeue(Interaction interaction) {
        return store.get(interaction.getQueueId()).remove(interaction);
    }
}
