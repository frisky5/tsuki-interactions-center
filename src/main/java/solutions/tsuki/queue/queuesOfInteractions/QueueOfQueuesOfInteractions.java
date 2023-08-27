package solutions.tsuki.queue.queuesOfInteractions;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;
import solutions.tsuki.queue.interactions.QueueOfInteractions;
import solutions.tsuki.queueItems.comparators.queue.interactionsQueue.TypeOne;

@ApplicationScoped
public class QueueOfQueuesOfInteractions {

  public ArrayList<QueueOfInteractions> queue = new ArrayList<>(10);
  public Comparator<QueueOfInteractions> comparator = new TypeOne();
  public ReentrantLock lock = new ReentrantLock(true);

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

  public QueueOfInteractions getHead() {
    return queue.size() > 0 ? queue.get(0) : null;
  }

  public int size() {
    return queue.size();
  }

  public void remove(QueueOfInteractions queue) {
    this.queue.remove(queue);
  }

  public boolean contains(QueueOfInteractions queue) {
    return this.queue.contains(queue);
  }

  public int sortedInsert(QueueOfInteractions queueToInsert) {
    if (queue.size() == 0) {
      queue.add(queueToInsert);
      return 0;
    } else {
      int insertIndex =
          -1 * (Collections.binarySearch(queue, queueToInsert,
              comparator)
              + 1);
      queue.add(insertIndex, queueToInsert);
      return insertIndex;
    }
  }
}
