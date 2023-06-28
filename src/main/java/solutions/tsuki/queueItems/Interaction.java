package solutions.tsuki.queueItems;

import solutions.tsuki.utils.timeMeasurements.InteractionTimeMeasurements;

import java.util.concurrent.locks.ReentrantLock;


public class Interaction {

    public final ReentrantLock lock = new ReentrantLock(true);
    public final InteractionTimeMeasurements timeMeasurements = new InteractionTimeMeasurements();
    public final Long id;
    public Integer queueId;
    public Integer priority;
    public Integer state;
    public Integer type;

    public Interaction(Long id) {
        this.id = id;
    }

    public InteractionTimeMeasurements getTimeMeasurements() {
        return timeMeasurements;
    }


    public Long getId() {
        return id;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Interaction{" +
                "timeMeasurements=" + timeMeasurements +
                ", id=" + id +
                ", queueId=" + queueId +
                ", priority=" + priority +
                ", state=" + state +
                ", type=" + type +
                '}';
    }
}
