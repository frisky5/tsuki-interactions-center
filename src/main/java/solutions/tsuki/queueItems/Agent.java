package solutions.tsuki.queueItems;

import solutions.tsuki.queues.QueueOfInteractions;
import solutions.tsuki.utils.timeMeasurements.AgentTimeMeasurements;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Agent {

    public final HashMap<Integer, QueueOfInteractions> assignedInteractionsQueues = new HashMap<>();
    public final HashMap<Long, Interaction> assignedInteractions = new HashMap<>();
    public final HashMap<Integer, Integer> prioritiesOnQueues = new HashMap<>();
    public final AgentTimeMeasurements timeMeasurements = new AgentTimeMeasurements();
    public final String id;
    public Integer state;
    public final Lock lock = new ReentrantLock(true);

    public Agent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public AgentTimeMeasurements getTimeMeasurements() {
        return timeMeasurements;
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

    public HashMap<Integer, QueueOfInteractions> getAssignedInteractionsQueues() {
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
