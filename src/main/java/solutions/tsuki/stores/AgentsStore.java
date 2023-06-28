package solutions.tsuki.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import solutions.tsuki.queueItems.Agent;

@ApplicationScoped
public class AgentsStore {

  public Map<String, Agent> store = new ConcurrentHashMap<>();

  public void put(Agent agent) {
    store.put(agent.getId(), agent);
  }

  public Agent get(String id) {
    return store.get(id);
  }
}
