package solutions.tsuki.queues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Agent;

public class QueueOfAgents {

  public ArrayList<Agent> queue = new ArrayList<>(25);
  public Logger logger;
  public Integer id;
  public Integer logic;
  public Comparator<Agent> comparator;

  public QueueOfAgents(Integer id, Comparator<Agent> comparator) {
    this.id = id;
    this.comparator = comparator;
    logger = LoggerFactory.getLogger("QueueOfAgents - " + this.id);
  }

  public Integer getId() {
    return id;
  }

  public Integer getLogic() {
    return logic;
  }

  public void setLogic(Integer logic) {
    this.logic = logic;
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

  public void remove(Agent agent) {
    queue.remove(agent);
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
