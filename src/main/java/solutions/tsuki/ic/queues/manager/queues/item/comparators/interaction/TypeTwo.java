package solutions.tsuki.ic.queues.manager.queues.item.comparators.interaction;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Comparator;
import solutions.tsuki.ic.queues.manager.queues.item.Interaction;


@ApplicationScoped
public class TypeTwo implements Comparator<Interaction> {

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
