package interactions_center.agents_manager.json.requests.changeState;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class ChangeAgentStateRequest {
    private String agentId;
    private Integer notReadyCode;
    private final LocalDateTime requestedAt = LocalDateTime.now(ZoneId.systemDefault());

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Integer getNotReadyCode() {
        return notReadyCode;
    }

    public void setNotReadyCode(Integer notReadyCode) {
        this.notReadyCode = notReadyCode;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
