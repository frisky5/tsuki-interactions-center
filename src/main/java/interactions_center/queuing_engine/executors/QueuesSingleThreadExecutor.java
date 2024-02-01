package interactions_center.queuing_engine.executors;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@ApplicationScoped
public class QueuesSingleThreadExecutor {
    private final Executor executor;

    public QueuesSingleThreadExecutor() {
        ThreadFactory singleThreadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("QSTE");
            return thread;
        };
        executor = Executors.newSingleThreadExecutor(singleThreadFactory);
    }

    public Executor getExecutor() {
        return executor;
    }
}
