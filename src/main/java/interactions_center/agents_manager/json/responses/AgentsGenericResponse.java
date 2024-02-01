package interactions_center.agents_manager.json.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentsGenericResponse {

    private Boolean error = false;
    private Integer code;
    private String message;
    private Long eventId;
    private Integer state;
    private String stateName;
    private Integer notReadyReasonId;
    private String notReadyReasonName;

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Integer getNotReadyReasonId() {
        return notReadyReasonId;
    }

    public void setNotReadyReasonId(Integer notReadyReasonId) {
        this.notReadyReasonId = notReadyReasonId;
    }

    public String getNotReadyReasonName() {
        return notReadyReasonName;
    }

    public void setNotReadyReasonName(String notReadyReasonName) {
        this.notReadyReasonName = notReadyReasonName;
    }
}
