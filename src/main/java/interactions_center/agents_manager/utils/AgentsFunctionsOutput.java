package interactions_center.agents_manager.utils;

public class AgentsFunctionsOutput {
    private Boolean error = false;
    private String message;
    private Integer code;
    private Long eventId;
    private Integer insertedAt;

    public Boolean isError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }


    public Integer getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(Integer insertedAt) {
        this.insertedAt = insertedAt;
    }
}
