package interactions_center.agents_manager.json.requests.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidateWsId {
    @JsonProperty(required = true)
    private String wsId;

    public String getWsId() {
        return wsId;
    }

    public void setWsId(String wsId) {
        this.wsId = wsId;
    }
}
