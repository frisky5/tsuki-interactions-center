package solutions.tsuki.queue.interactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.utils.timeMeasurements.InteractionsQueueTimeMeasurement;


public class QueueOfInteractions {

  public final ArrayList<Interaction> queueOfInteractions = new ArrayList<>();
  public final Integer id;
  public Integer logic;
  public Integer type;
  public final InteractionsQueueTimeMeasurement timeMeasurement = new InteractionsQueueTimeMeasurement();
  public Comparator<Interaction> comparator;
  public final ReentrantLock lock = new ReentrantLock(true);

  public QueueOfInteractions(Integer id, Comparator<Interaction> comparator) {
    this.id = id;
    this.comparator = comparator;
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

  public Integer getId() {
    return id;
  }

  public int size() {
    return queueOfInteractions.size();
  }

  public InteractionsQueueTimeMeasurement getTimeMeasurement() {
    return timeMeasurement;
  }

  public Interaction getHead() {
    return queueOfInteractions.size() > 0 ? queueOfInteractions.get(0) : null;
  }

  public int sortedInsert(Interaction interaction) {
    if (queueOfInteractions.contains(interaction)) {
      return -1;
    }
    if (queueOfInteractions.size() == 0) {
      queueOfInteractions.add(interaction);
      return 0;
    } else {
      int insertIndex =
          -1 * (Collections.binarySearch(queueOfInteractions, interaction,
              comparator)
              + 1);
      queueOfInteractions.add(insertIndex, interaction);
      return insertIndex;
    }
  }

  public boolean remove(Interaction interaction) {
    return queueOfInteractions.remove(interaction);
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
    return "{id=" + id + ", queue=" + queueOfInteractions + "}";
  }
}
