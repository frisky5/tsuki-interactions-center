package interactions_center.interactions_manager.utils;

import java.time.LocalDateTime;

public class InteractionTimeMeasurements {

    private LocalDateTime createdAt;
    private LocalDateTime queuedAt;
    private LocalDateTime offeredAt;
    private LocalDateTime answeredAt;

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

    public LocalDateTime getOfferedAt() {
        return offeredAt;
    }

    public void setOfferedAt(LocalDateTime offeredAt) {
        this.offeredAt = offeredAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    @Override
    public String toString() {
        return "InteractionTimeMeasurements{" +
                "createdAt=" + createdAt +
                ", queuedAt=" + queuedAt +
                '}';
    }
}
