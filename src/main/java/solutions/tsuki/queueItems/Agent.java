package solutions.tsuki.queueItems;

import solutions.tsuki.queue.interactions.QueueOfInteractions;
import solutions.tsuki.utils.timeMeasurements.AgentTimeMeasurements;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class Agent {

    public final ConcurrentHashMap<Integer, QueueOfInteractions> assignedInteractionsQueues = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Long, Interaction> assignedInteractions = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Integer, Integer> prioritiesOnQueues = new ConcurrentHashMap<>();
    public final AgentTimeMeasurements timeMeasurements = new AgentTimeMeasurements();
    public final String id;
    public AtomicInteger state = new AtomicInteger(0);
    public final ReentrantLock lock = new ReentrantLock(true);

    public Agent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
        prioritiesOnQueues.put(queue.getId(), priority);
    }

    public void assignInteraction(Interaction interaction) {
        assignedInteractions.put(interaction.getId(), interaction);
    }

    public Interaction removeInteraction(Interaction interaction) {
        return assignedInteractions.remove(interaction.getId());
    }

    public ConcurrentHashMap<Integer, QueueOfInteractions> getAssignedInteractionsQueues() {
        return assignedInteractionsQueues;
    }

    public Integer getPriorityOnQueue(Integer queueId) {
        return prioritiesOnQueues.getOrDefault(queueId, 100);
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
