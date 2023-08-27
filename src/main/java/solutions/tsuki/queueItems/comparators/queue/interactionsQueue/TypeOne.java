package solutions.tsuki.queueItems.comparators.queue.interactionsQueue;

import solutions.tsuki.queue.interactions.QueueOfInteractions;

import java.util.Comparator;


public class TypeOne implements Comparator<QueueOfInteractions> {

    @Override
    public int compare(QueueOfInteractions t1, QueueOfInteractions t2) {
        if (t1.getTimeMeasurement().getLastOfferedFrom().isBefore(t2.getTimeMeasurement()
                .getLastOfferedFrom())) {
            return -1;
        }
        return 1;
    }
}
