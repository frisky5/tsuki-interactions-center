package interactions_center.interactions_manager.stores.items;

import interactions_center.interactions_manager.utils.InteractionTimeMeasurements;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class Interaction {
    public final Long id;
    public UUID queueId;
    public Integer priorityOnQueue;
    public final InteractionTimeMeasurements timeMeasurements = new InteractionTimeMeasurements();
    public AtomicInteger state = new AtomicInteger(0);
    public Integer type;
    public final ReentrantLock lock = new ReentrantLock(true);

    public Interaction(Long id) {
        this.id = id;
    }

    public InteractionTimeMeasurements getTimeMeasurements() {
        return timeMeasurements;
    }

    public Long getId() {
        return id;
    }

    public UUID getQueueId() {
        return queueId;
    }

    public void setQueueId(UUID queueId) {
        this.queueId = queueId;
    }

    public Integer getPriorityOnQueue() {
        return priorityOnQueue;
    }

    public void setPriorityOnQueue(Integer priorityOnQueue) {
        this.priorityOnQueue = priorityOnQueue;
    }

    public Integer getState() {
        return state.get();
    }

    public void setState(Integer state) {
        this.state.set(state);
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    @Override
    public String toString() {
        return "Interaction{" +
                "timeMeasurements=" + timeMeasurements +
                ", id=" + id +
                ", queueId=" + queueId +
                ", priority=" + priorityOnQueue +
                ", state=" + state +
                ", type=" + type +
                '}';
    }
}
