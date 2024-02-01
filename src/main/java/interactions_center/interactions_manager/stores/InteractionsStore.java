package interactions_center.interactions_manager.stores;

import interactions_center.interactions_manager.stores.items.Interaction;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InteractionsStore {

  public ConcurrentHashMap<Long, Interaction> store = new ConcurrentHashMap<>();
  public ConcurrentHashMap<Long, Interaction> offeringInteractions = new ConcurrentHashMap<>();


  public void put(Interaction interaction) {
    store.put(interaction.getId(), interaction);
  }

  public Interaction get(Long id) {
    return store.get(id);
  }

  public void putOffering(Interaction interaction) {
    offeringInteractions.put(interaction.getId(), interaction);
  }

  public Interaction getOffering(Long id) {
    return offeringInteractions.get(id);
  }

  @Override
  public String toString() {
    return "InteractionsStore{" + "interactionsStore=" + store + '}';
  }

  public boolean contains(Long id) {
    return store.containsKey(id);
  }

  public Set<Long> getKeySet() {
    return store.keySet();
  }
}
