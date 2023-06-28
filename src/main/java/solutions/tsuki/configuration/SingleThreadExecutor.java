package solutions.tsuki.configuration;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@ApplicationScoped
public class SingleThreadExecutor {

    public final ExecutorService executor;

    public SingleThreadExecutor() {
        ThreadFactory customThreadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("STE");
            return thread;
        };
        executor = Executors.newSingleThreadExecutor(customThreadFactory);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

}
