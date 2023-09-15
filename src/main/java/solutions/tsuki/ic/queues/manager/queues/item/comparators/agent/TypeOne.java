package solutions.tsuki.ic.queues.manager.queues.item.comparators.agent;

import solutions.tsuki.ic.queues.manager.queues.item.Agent;

import java.util.Comparator;


public class TypeOne implements Comparator<Agent> {

    private final Integer queuedId;

    public TypeOne(Integer queuedId) {
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
        if (agent1.getTimeMeasurements().getIdleAt()
                .isBefore(agent2.getTimeMeasurements().getIdleAt())) {
            return -1;
        }
        return 1;
    }
}
