package interactions_center.queuing_engine.utils;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@ApplicationScoped
public class ExecutorsFactory {

  public final Executor qqiSingleThreadExecutor;
  public final Executor databaseMultiThreadExecutor;
  public final Executor multiThreadExecutor;

  public ExecutorsFactory() {
    ThreadFactory singleThreadFactory = runnable -> {
      Thread thread = new Thread(runnable);
      thread.setName("QQI-STE");
      return thread;
    };

    ThreadFactory multiThreadFactory = runnable -> {
      Thread thread = new Thread(runnable);
      thread.setName("MTE");
      return thread;
    };

    ThreadFactory databaseThreadFactory = runnable -> {
      Thread thread = new Thread(runnable);
      thread.setName("DB-MTE");
      return thread;
    };

    qqiSingleThreadExecutor = Executors.newSingleThreadExecutor(singleThreadFactory);
    databaseMultiThreadExecutor = Executors.newFixedThreadPool(5, databaseThreadFactory);
    multiThreadExecutor = Executors.newFixedThreadPool(5, multiThreadFactory);
  }

  public Executor getQqiSingleThreadExecutor() {
    return qqiSingleThreadExecutor;
  }

  public Executor getDatabaseMultiThreadExecutor() {
    return databaseMultiThreadExecutor;
  }

  public Executor getMultiThreadExecutor() {
    return multiThreadExecutor;
  }
}
