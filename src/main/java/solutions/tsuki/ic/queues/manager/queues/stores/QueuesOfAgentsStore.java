package solutions.tsuki.ic.queues.manager.queues.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;

@ApplicationScoped
public class QueuesOfAgentsStore {

  public final ConcurrentHashMap<Integer, QueueOfAgents> store = new ConcurrentHashMap<>(20);

  public void putQueue(QueueOfAgents queue) {
    store.put(queue.getId(), queue);
  }

  public QueueOfAgents get(Integer id) {
    return store.get(id);
  }

}
