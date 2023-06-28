package solutions.tsuki.utils.timeMeasurements;

import java.time.LocalDateTime;

public class InteractionTimeMeasurements {

    public LocalDateTime createdAt;
    public LocalDateTime queuedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(LocalDateTime queuedAt) {
        this.queuedAt = queuedAt;
    }

    @Override
    public String toString() {
        return "InteractionTimeMeasurements{" +
                "createdAt=" + createdAt +
                ", queuedAt=" + queuedAt +
                '}';
    }
}
