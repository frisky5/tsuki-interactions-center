package solutions.tsuki.utils.timeMeasurements;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class InteractionsQueueTimeMeasurement {

    public LocalDateTime lastOfferedFrom = LocalDateTime.now(ZoneId.of("UTC"));

    public LocalDateTime getLastOfferedFrom() {
        return lastOfferedFrom;
    }

    public void setLastOfferedFrom(LocalDateTime lastOfferedFrom) {
        this.lastOfferedFrom = lastOfferedFrom;
    }
}
