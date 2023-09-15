package solutions.tsuki.ic.queues.manager.json.requests.queue;

public class QueueRequest {

  private Integer id;
  private String name;
  private String createdAt;
  private String lastUpdatedAt;
  private Integer type;
  private String typeName;
  private Integer interactionQueuingLog;
  private Integer agentQueuingLogic;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  public void setLastUpdatedAt(String lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public Integer getInteractionQueuingLog() {
    return interactionQueuingLog;
  }

  public void setInteractionQueuingLog(Integer interactionQueuingLog) {
    this.interactionQueuingLog = interactionQueuingLog;
  }

  public Integer getAgentQueuingLogic() {
    return agentQueuingLogic;
  }

  public void setAgentQueuingLogic(Integer agentQueuingLogic) {
    this.agentQueuingLogic = agentQueuingLogic;
  }
}
