package solutions.tsuki.ic.interactions.manager.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import solutions.tsuki.ic.queues.manager.queues.item.Interaction;

@ApplicationScoped
public class InteractionsStore {

  public ConcurrentHashMap<Long, Interaction> interactionsStore = new ConcurrentHashMap<>();
  public ConcurrentHashMap<Long, Interaction> offeringInteractions = new ConcurrentHashMap<>();


  public void put(Interaction interaction) {
    interactionsStore.put(interaction.getId(), interaction);
  }

  public Interaction get(Long id) {
    return interactionsStore.get(id);
  }

  public void putOffering(Interaction interaction) {
    offeringInteractions.put(interaction.getId(), interaction);
  }

  public Interaction getOffering(Long id) {
    return offeringInteractions.get(id);
  }

  @Override
  public String toString() {
    return "InteractionsStore{" + "interactionsStore=" + interactionsStore + '}';
  }

  public boolean contains(Long id) {
    return interactionsStore.containsKey(id);
  }
}
