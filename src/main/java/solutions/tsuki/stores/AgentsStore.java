package solutions.tsuki.stores;

import jakarta.enterprise.context.ApplicationScoped;
import solutions.tsuki.queueItems.Agent;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AgentsStore {

    public ConcurrentHashMap<String, Agent> store = new ConcurrentHashMap<>();

    public void put(Agent agent) {
        store.put(agent.getId(), agent);
    }

    public Agent get(String id) {
        return store.get(id);
    }

    public boolean contains(String id) {
        return store.containsKey(id);
    }
}
