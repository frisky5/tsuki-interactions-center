package solutions.tsuki.ic.queues.manager.queues.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import solutions.tsuki.ic.queues.manager.queues.interactions.QueueOfInteractions;

@ApplicationScoped
public class QueuesOfInteractionsStore {

  public final ConcurrentHashMap<Integer, QueueOfInteractions> store = new ConcurrentHashMap<>(20);

  public void putQueue(QueueOfInteractions queue) {
    store.put(queue.getId(), queue);
  }

  public QueueOfInteractions get(Integer id) {
    return store.get(id);
  }

  public boolean contains(Integer id) {
    return store.containsKey(id);
  }
}
