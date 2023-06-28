package solutions.tsuki.functions.queue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import solutions.tsuki.queues.QueueOfQueuesOfInteractions;


@ApplicationScoped
public class ProcessQueueOfQueuesOfInteractionsFunction implements Function<Void, Void> {

  @Inject
  QueueOfQueuesOfInteractions queueOfQueuesOfInteractions;

  @Override
  public Void apply(Void unused) {
    return null;
  }
}
