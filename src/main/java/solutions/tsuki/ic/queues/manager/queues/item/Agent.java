package solutions.tsuki.ic.queues.manager.queues.item;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import solutions.tsuki.ic.queues.manager.queues.interactions.QueueOfInteractions;
import solutions.tsuki.utils.timeMeasurements.AgentTimeMeasurements;


public class Agent {

  private final UUID keycloakUserUuid;
  private final ConcurrentHashMap<Integer, QueueOfInteractions> assignedInteractionsQueues = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, Integer> interactionsQueuesPriorities = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Long, Interaction> assignedInteractions = new ConcurrentHashMap<>();
  private final AgentTimeMeasurements timeMeasurements = new AgentTimeMeasurements();
  private AtomicInteger state = new AtomicInteger(0);
  private final ReentrantLock lock = new ReentrantLock(true);

  public Agent(UUID id) {
    this.keycloakUserUuid = id;
  }

  public UUID getKeycloakUserUuid() {
    return keycloakUserUuid;
  }

  public Integer getState() {
    return state.get();
  }

  public void setState(Integer state) {
    this.state.set(state);
  }

  public AgentTimeMeasurements getTimeMeasurements() {
    return timeMeasurements;
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

  public void assignQueue(QueueOfInteractions queue, Integer priority) {
    assignedInteractionsQueues.put(queue.getId(), queue);
    interactionsQueuesPriorities.put(queue.getId(), priority);
  }

  public void assignInteraction(Interaction interaction) {
    assignedInteractions.put(interaction.getId(), interaction);
  }

  public Interaction unassignInteraction(Interaction interaction) {
    return assignedInteractions.remove(interaction.getId());
  }

  public ConcurrentHashMap<Integer, QueueOfInteractions> getAssignedInteractionsQueues() {
    return assignedInteractionsQueues;
  }

  public Integer getPriorityOnQueue(Integer queueId) {
    return interactionsQueuesPriorities.getOrDefault(queueId, 100);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Agent agent = (Agent) o;
    return Objects.equals(keycloakUserUuid, agent.keycloakUserUuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keycloakUserUuid);
  }

  @Override
  public String toString() {
    return "Agent{" +
        "id='" + keycloakUserUuid + '\'' +
        '}';
  }
}
