package interactions_center.agents_manager.json.requests;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Queue {
    private UUID id;
    private String name;
    private Integer priority;
    private Integer queueLogic;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getQueueLogic() {
        return queueLogic;
    }

    public void setQueueLogic(Integer queueLogic) {
        this.queueLogic = queueLogic;
    }
}
