package interactions_center.queuing_engine.json.requests;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class EnqueueInteractionRequest {
    private Long interactionId;
    private UUID queueId;
    private Integer priority;
    private final LocalDateTime requestedAt = LocalDateTime.now(ZoneId.systemDefault());

    public Long getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Long interactionId) {
        this.interactionId = interactionId;
    }

    public UUID getQueueId() {
        return queueId;
    }

    public void setQueueId(UUID queueId) {
        this.queueId = queueId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
