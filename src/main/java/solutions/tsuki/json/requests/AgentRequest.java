package solutions.tsuki.json.requests;

import java.time.LocalDateTime;

public class AgentRequest {

  public String id;
  public Integer state;
  public LocalDateTime requestAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state;
  }

  public LocalDateTime getRequestAt() {
    return requestAt;
  }

  public void setRequestAt(LocalDateTime requestAt) {
    this.requestAt = requestAt;
  }
}
