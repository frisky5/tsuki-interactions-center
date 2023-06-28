package solutions.tsuki.utils.timeMeasurements;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class AgentTimeMeasurements {

  public LocalDateTime loggedInAt = LocalDateTime.now(ZoneId.of("UTC"));
  public LocalDateTime idleAt = LocalDateTime.now(ZoneId.of("UTC"));
  public LocalDateTime queuedAt = LocalDateTime.now(ZoneId.of("UTC"));
  public LocalDateTime loggedOutAt = LocalDateTime.now(ZoneId.of("UTC"));
  public LocalDateTime updatedAt = LocalDateTime.now(ZoneId.of("UTC"));
  public LocalDateTime notReadyAt = LocalDateTime.now(ZoneId.of("UTC"));
  public LocalDateTime offeredAt = LocalDateTime.now(ZoneId.of("UTC"));

  public LocalDateTime getLoggedInAt() {
    return loggedInAt;
  }

  public void setLoggedInAt(LocalDateTime loggedInAt) {
    this.loggedInAt = loggedInAt;
  }

  public LocalDateTime getIdleAt() {
    return idleAt;
  }

  public void setIdleAt(LocalDateTime idleAt) {
    this.idleAt = idleAt;
  }

  public LocalDateTime getQueuedAt() {
    return queuedAt;
  }

  public void setQueuedAt(LocalDateTime queuedAt) {
    this.queuedAt = queuedAt;
  }

  public LocalDateTime getLoggedOutAt() {
    return loggedOutAt;
  }

  public void setLoggedOutAt(LocalDateTime loggedOutAt) {
    this.loggedOutAt = loggedOutAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public LocalDateTime getNotReadyAt() {
    return notReadyAt;
  }

  public void setNotReadyAt(LocalDateTime notReadyAt) {
    this.notReadyAt = notReadyAt;
  }

  public LocalDateTime getOfferedAt() {
    return offeredAt;
  }

  public void setOfferedAt(LocalDateTime offeredAt) {
    this.offeredAt = offeredAt;
  }
}
