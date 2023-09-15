package solutions.tsuki.ic.agents.manager.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AgentsStore {

    public ConcurrentHashMap<UUID, Agent> store = new ConcurrentHashMap<>();

    public void put(Agent agent) {
        store.put(agent.getKeycloakUserUuid(), agent);
    }

    public Agent get(UUID id) {
        return store.get(id);
    }

    public boolean contains(String id) {
        return store.containsKey(id);
    }
}
