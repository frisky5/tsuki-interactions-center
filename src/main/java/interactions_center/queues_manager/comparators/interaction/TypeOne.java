package interactions_center.queues_manager.comparators.interaction;

import interactions_center.interactions_manager.stores.items.Interaction;

import java.util.Comparator;
import java.util.UUID;


public class TypeOne implements Comparator<Interaction> {
    private final UUID queuedId;

    public TypeOne(UUID queuedId) {
        this.queuedId = queuedId;
    }

    /*
     * Below is the comparison order
     * 1) compare priority
     * 2) compare created at
     * 3) compare queued at
     */
    @Override
    public int compare(Interaction arg0, Interaction arg1) {
        if (arg0.getPriorityOnQueue() > arg1.getPriorityOnQueue()) {
            return -1;
        }
        if (arg0.getTimeMeasurements().getCreatedAt()
                .isBefore(arg1.getTimeMeasurements().getCreatedAt())) {
            return -1;
        }
        return 1;
    }
}
