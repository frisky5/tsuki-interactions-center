package interactions_center.agents_manager.stores.items;

import interactions_center.agents_manager.constants.AGENT_STATES;
import interactions_center.interactions_manager.stores.items.Interaction;
import interactions_center.queues_manager.queues.QueueOfInteractions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


public class Agent {
    private final ReentrantLock lock = new ReentrantLock(true);

    private final UUID id;
    private final ConcurrentHashMap<UUID, QueueOfInteractions> assignedInteractionsQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> interactionsQueuesPriorities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Interaction> assignedInteractions = new ConcurrentHashMap<>();

    private LocalDateTime lastStateChangedAt = LocalDateTime.now(ZoneId.systemDefault());
    private Integer state = AGENT_STATES.LOGOUT;
    private Integer notReadyCode = -1;


    public Agent(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getNotReadyCode() {
        return notReadyCode;
    }

    public void setNotReadyCode(Integer notReadyCode) {
        this.notReadyCode = notReadyCode;
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

    public ConcurrentHashMap<UUID, QueueOfInteractions> getAssignedInteractionsQueues() {
        return assignedInteractionsQueues;
    }

    public Integer getPriorityOnQueue(UUID queueId) {
        return interactionsQueuesPriorities.getOrDefault(queueId, 100);
    }

    public LocalDateTime getLastStateChangedAt() {
        return lastStateChangedAt;
    }

    public void setLastStateChangedAt(LocalDateTime lastStateChangedAt) {
        this.lastStateChangedAt = lastStateChangedAt;
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
        return Objects.equals(id, agent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id='" + id + '\'' +
                '}';
    }
}
