package solutions.tsuki.ic.queues.manager.functions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.functions.agent.state.OfferingInteractionFunction;
import solutions.tsuki.ic.queues.manager.functions.queueOfQueuesOfInteractions.ValidateByQueueFunction;
import solutions.tsuki.ic.queues.manager.queues.interactions.QueueOfInteractions;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;


@ApplicationScoped
public class ProcessQueuesOfInteractions implements Function<QueueOfInteractions, Void> {

  public final Logger LOG = LoggerFactory.getLogger(ProcessQueuesOfInteractions.class);
  @Inject
  QueuesStores queuesStores;

  @Inject
  OfferingInteractionFunction offeringInteractionFunction;

  @Inject
  ValidateByQueueFunction validateByQueueFunction;

  @Inject
  Scheduler quartz;

  @Override
  public Void apply(QueueOfInteractions queueToProcess) {
    return null;
  }
}
