package solutions.tsuki.ic.queues.manager.queues.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import solutions.tsuki.ic.queues.manager.queues.queuesOfInteractions.QueueOfQueuesOfInteractions;
import solutions.tsuki.ic.queues.manager.stores.AgentsWebsocketSessionsStore;

@ApplicationScoped
public class QueuesStores {


  @Inject
  QueuesOfAgentsStore queuesOfAgentsStore;
  @Inject
  QueuesOfInteractionsStore queuesOfInteractionsStore;
  @Inject
  QueueOfQueuesOfInteractions queueOfQueuesOfInteractions;
  @Inject
  AgentsWebsocketSessionsStore agentsWebsocketSessionsStore;

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
