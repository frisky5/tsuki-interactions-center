package solutions.tsuki.ic.queues.manager.queues.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;

public class QueueOfAgents {

  private final ArrayList<Agent> queue = new ArrayList<>(25);
  private final Integer id;
  private Integer logic;
  private Integer type;
  private String name;
  private Comparator<Agent> comparator;
  private final ReentrantLock lock = new ReentrantLock(true);

  public QueueOfAgents(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public ArrayList<Agent> getQueue() {
    return queue;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getLogic() {
    return logic;
  }

  public void setLogic(Integer logic) {
    this.logic = logic;
  }

  public Comparator<Agent> getComparator() {
    return comparator;
  }

  public void setComparator(
      Comparator<Agent> comparator) {
    this.comparator = comparator;
  }

  public ReentrantLock getLock() {
    return lock;
  }

  public int sortedInsert(Agent agent) {
    if (queue.size() == 0) {
      queue.add(agent);
      return 0;
    } else {
      int insertIndex =
          -1 * (Collections.binarySearch(queue, agent,
              comparator)
              + 1);
      queue.add(insertIndex, agent);
      return insertIndex;
    }
  }

  public boolean remove(Agent agent) {
    return queue.remove(agent);
  }

  public int size() {
    return queue.size();
  }

  public Agent getHead() {
    if (queue.size() > 0) {
      return queue.get(0);
    } else {
      return null;
    }
  }

  public Agent getTail() {
    if (queue.size() > 0) {
      return queue.get(size() - 1);
    } else {
      return null;
    }
  }

  public boolean contains(Agent agent) {
    return queue.contains(agent);
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueueOfAgents that = (QueueOfAgents) o;
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
