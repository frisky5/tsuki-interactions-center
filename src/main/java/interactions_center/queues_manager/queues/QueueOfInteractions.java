package interactions_center.queues_manager.queues;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import interactions_center.interactions_manager.stores.items.Interaction;
import solutions.tsuki.utils.timeMeasurements.InteractionsQueueTimeMeasurement;


public class QueueOfInteractions {

  public final ArrayList<Interaction> queue = new ArrayList<>();
  private final UUID id;
  private final Integer type;
  private String name;

  private final InteractionsQueueTimeMeasurement timeMeasurement = new InteractionsQueueTimeMeasurement();
  private Comparator<Interaction> comparator;
  private final ReentrantLock lock = new ReentrantLock(true);

  public QueueOfInteractions(UUID id, Integer type) {
    this.id = id;
    this.type = type;
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getType() {
    return type;
  }

  public Comparator<Interaction> getComparator() {
    return comparator;
  }

  public void setComparator(Comparator<Interaction> comparator) {
    this.comparator = comparator;
  }

  public int size() {
    return queue.size();
  }

  public InteractionsQueueTimeMeasurement getTimeMeasurement() {
    return timeMeasurement;
  }

  public Interaction getHead() {
    return queue.size() > 0 ? queue.get(0) : null;
  }

  public int sortedInsert(Interaction interaction) {
    if (queue.contains(interaction)) {
      return -1;
    }
    if (queue.size() == 0) {
      queue.add(interaction);
      return 0;
    } else {
      int insertIndex =
          -1 * (Collections.binarySearch(queue, interaction,
              comparator)
              + 1);
      queue.add(insertIndex, interaction);
      return insertIndex;
    }
  }

  public boolean remove(Interaction interaction) {
    return queue.remove(interaction);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueueOfInteractions that = (QueueOfInteractions) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "{id=" + id + ", queue=" + queue + "}";
  }
}
