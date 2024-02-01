package interactions_center.agents_manager.json.requests.websocket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WsMessage {

    @JsonIgnore
    private final LocalDateTime requestedAt = LocalDateTime.now(ZoneId.systemDefault());
    private Boolean error;
    private Integer cmd;
    private Integer subCmd;
    private Integer code;
    private Long interactionId;
    @JsonIgnore
    private UUID requestingAgentUuid;
    private String message;
    private String wsId;
    private Integer state;
    private Integer notReadyReason;
    private String reqId;

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Integer getCmd() {
        return cmd;
    }

    public void setCmd(Integer cmd) {
        this.cmd = cmd;
    }

    public Integer getSubCmd() {
        return subCmd;
    }

    public void setSubCmd(Integer subCmd) {
        this.subCmd = subCmd;
    }

    public Long getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Long interactionId) {
        this.interactionId = interactionId;
    }

    public UUID getRequestingAgentUuid() {
        return requestingAgentUuid;
    }

    public void setRequestingAgentUuid(UUID requestingAgentUuid) {
        this.requestingAgentUuid = requestingAgentUuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWsId() {
        return wsId;
    }

    public void setWsId(String wsId) {
        this.wsId = wsId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getNotReadyReason() {
        return notReadyReason;
    }

    public void setNotReadyReason(Integer notReadyReason) {
        this.notReadyReason = notReadyReason;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }
}
