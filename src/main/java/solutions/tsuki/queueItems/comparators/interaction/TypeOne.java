package solutions.tsuki.queueItems.comparators.interaction;

import solutions.tsuki.queueItems.Interaction;

import java.util.Comparator;


public class TypeOne implements Comparator<Interaction> {

    /*
     * Below is the comparison order
     * 1) compare priority
     * 2) compare created at
     * 3) compare queued at
     */
    @Override
    public int compare(Interaction arg0, Interaction arg1) {
        if (arg0.getPriority() > arg1.getPriority()) {
            return -1;
        }
        if (arg0.getTimeMeasurements().getCreatedAt()
                .isBefore(arg1.getTimeMeasurements().getCreatedAt())) {
            return -1;
        }
        return 1;
    }
}
