package solutions.tsuki.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class MultiThreadExecutor {

  public ExecutorService executor = Executors.newFixedThreadPool(5);

  public ExecutorService getExecutor() {
    return executor;
  }

}
