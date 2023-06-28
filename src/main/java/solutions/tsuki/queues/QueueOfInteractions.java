package solutions.tsuki.queues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.queueItems.comparators.interaction.TypeOne;
import solutions.tsuki.utils.timeMeasurements.InteractionsQueueTimeMeasurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


public class QueueOfInteractions {

    public ReentrantLock lock = new ReentrantLock(true);
    public ArrayList<Interaction> queueOfInteractions = new ArrayList<>();
    public Logger logger;
    public Integer id;
    public Integer logic;
    public Integer type;
    public InteractionsQueueTimeMeasurement timeMeasurement = new InteractionsQueueTimeMeasurement();
    public Comparator<Interaction> comparator = new TypeOne();

    public QueueOfInteractions(Integer id, Comparator<Interaction> comparator) {
        this.id = id;
        this.comparator = comparator;
        logger = LoggerFactory.getLogger("QueueOfInteractions - " + this.id);
    }

    public Integer getId() {
        return id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLogic() {
        return logic;
    }

    public void setLogic(Integer logic) {
        this.logic = logic;
    }

    public int size() {
        return queueOfInteractions.size();
    }

    public InteractionsQueueTimeMeasurement getTimeMeasurement() {
        return timeMeasurement;
    }

    public Interaction getHead() {
        if (queueOfInteractions.size() > 0) {
            return queueOfInteractions.get(0);
        } else {
            return null;
        }
    }

    public Interaction getTail() {
        if (queueOfInteractions.size() > 0) {
            return queueOfInteractions.get(size() - 1);
        } else {
            return null;
        }
    }

    public void setTimeMeasurement(
            InteractionsQueueTimeMeasurement timeMeasurement) {
        this.timeMeasurement = timeMeasurement;
    }

    public int sortedInsert(Interaction interaction) {
        if (queueOfInteractions.contains(interaction)) {
            return -1;
        }
        if (queueOfInteractions.size() == 0) {
            queueOfInteractions.add(interaction);
            return 0;
        } else {
            int insertIndex =
                    -1 * (Collections.binarySearch(queueOfInteractions, interaction,
                            comparator)
                            + 1);
            queueOfInteractions.add(insertIndex, interaction);
            return insertIndex;
        }
    }

    public boolean remove(Interaction interaction) {
        boolean result = queueOfInteractions.remove(interaction);
        logger.info("result of removing interaction [{}] from queue [{}] is [{}]", interaction.getId(),
                interaction.getQueueId(), result);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueueOfInteractions that = (QueueOfInteractions) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "{id=" + id + ", queue=" + queueOfInteractions + "}";
    }
}
