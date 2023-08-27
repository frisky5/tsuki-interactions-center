package solutions.tsuki.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import solutions.tsuki.queue.queuesOfInteractions.QueueOfQueuesOfInteractions;

@ApplicationScoped
public class StoresDTO {

  @Inject
  AgentsStore agentsStore;
  @Inject
  InteractionsStore interactionsStore;
  @Inject
  QueuesOfAgentsStore queuesOfAgentsStore;
  @Inject
  QueuesOfInteractionsStore queuesOfInteractionsStore;
  @Inject
  QueueOfQueuesOfInteractions queueOfQueuesOfInteractions;
  @Inject
  AgentsWebsocketSessionsStore agentsWebsocketSessionsStore;

  public AgentsStore getAgentsStore() {
    return agentsStore;
  }

  public void setAgentsStore(AgentsStore agentsStore) {
    this.agentsStore = agentsStore;
  }

  public InteractionsStore getInteractionsStore() {
    return interactionsStore;
  }

  public void setInteractionsStore(InteractionsStore interactionsStore) {
    this.interactionsStore = interactionsStore;
  }

  public QueuesOfAgentsStore getQueuesOfAgentsStore() {
    return queuesOfAgentsStore;
  }

  public void setQueuesOfAgentsStore(QueuesOfAgentsStore queuesOfAgentsStore) {
    this.queuesOfAgentsStore = queuesOfAgentsStore;
  }

  public QueuesOfInteractionsStore getQueuesOfInteractionsStore() {
    return queuesOfInteractionsStore;
  }

  public void setQueuesOfInteractionsStore(QueuesOfInteractionsStore queuesOfInteractionsStore) {
    this.queuesOfInteractionsStore = queuesOfInteractionsStore;
  }

  public QueueOfQueuesOfInteractions getQueueOfQueuesOfInteractions() {
    return queueOfQueuesOfInteractions;
  }

  public void setQueueOfQueuesOfInteractions(
      QueueOfQueuesOfInteractions queueOfQueuesOfInteractions) {
    this.queueOfQueuesOfInteractions = queueOfQueuesOfInteractions;
  }

  public AgentsWebsocketSessionsStore getAgentsWebsocketSessionsStore() {
    return agentsWebsocketSessionsStore;
  }

  public void setAgentsWebsocketSessionsStore(
      AgentsWebsocketSessionsStore agentsWebsocketSessionsStore) {
    this.agentsWebsocketSessionsStore = agentsWebsocketSessionsStore;
  }
}
