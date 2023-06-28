package solutions.tsuki.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Interaction;

@ApplicationScoped
public class InteractionsStore {

  public final Logger logger = LoggerFactory.getLogger("InteractionsStore");
  public Map<Long, Interaction> interactionsStore = new ConcurrentHashMap<>();


  public void put(Interaction interaction) {
    interactionsStore.put(interaction.getId(), interaction);
  }

  public Interaction get(Long id) {
    return interactionsStore.get(id);
  }

  @Override
  public String toString() {
    return "InteractionsStore{" +
        "interactionsStore=" + interactionsStore +
        '}';
  }

  public void printStoreToLog() {
    logger.info(this.toString());
  }
}
