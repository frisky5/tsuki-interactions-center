package solutions.tsuki.ic.queues.manager.queues.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import solutions.tsuki.ic.agents.manager.stores.AgentsStore;
import solutions.tsuki.ic.interactions.manager.stores.InteractionsStore;

@ApplicationScoped
public class QueuesItemsStore {

  @Inject
  AgentsStore agentsStore;
  @Inject
  InteractionsStore interactionsStore;

  public AgentsStore getAgentsStore() {
    return agentsStore;
  }

  public void setAgentsStore(AgentsStore agentsStore) {
    this.agentsStore = agentsStore;
  }

  public InteractionsStore getInteractionsStore() {
    return interactionsStore;
  }

  public void setInteractionsStore(
      InteractionsStore interactionsStore) {
    this.interactionsStore = interactionsStore;
  }
}
