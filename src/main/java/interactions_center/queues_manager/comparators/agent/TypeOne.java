package interactions_center.queues_manager.comparators.agent;

import interactions_center.agents_manager.stores.items.Agent;

import java.util.Comparator;
import java.util.UUID;

public class TypeOne implements Comparator<Agent> {

    private final UUID queuedId;

    public TypeOne(UUID queuedId) {
        this.queuedId = queuedId;
    }

    /*
     * Below is the comparison order
     * 1) compare priority
     * 2) compare idle at
     */
    /*-1 is higher priority and 1 is lower priority, higher priority means inserted in the beginning of the queue*/
    @Override
    public int compare(Agent agent1, Agent agent2) {
        if (agent1.getPriorityOnQueue(queuedId) > agent2.getPriorityOnQueue(queuedId)) {
            return -1;
        }
        if (agent1.getLastStateChangedAt()
                .isBefore(agent2.getLastStateChangedAt())) {
            return -1;
        }
        return 1;
    }
}
