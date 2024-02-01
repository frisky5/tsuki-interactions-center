package interactions_center.queuing_engine.utils;

public class QueuingEngineOutput {
    private Boolean error = false;
    private String message;
    private Boolean code;
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

    public Boolean getCode() {
        return code;
    }

    public void setCode(Boolean code) {
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
