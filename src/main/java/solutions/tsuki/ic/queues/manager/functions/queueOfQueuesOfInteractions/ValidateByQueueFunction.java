package solutions.tsuki.ic.queues.manager.functions.queueOfQueuesOfInteractions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;
import solutions.tsuki.ic.queues.manager.queues.interactions.QueueOfInteractions;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;


@ApplicationScoped
public class ValidateByQueueFunction implements Function<Integer, Void> {

  public final Logger LOG = LoggerFactory.getLogger("QQI-ValidateByQueueFunction");

  @Inject
  QueuesStores queuesStores;

  @Override
  public Void apply(Integer queueId) {
    LOG.info("starting validation");
    QueueOfInteractions queueOfInteractionsToProcess = null;
    QueueOfAgents queueOfAgentsToProcess = null;
    try {
      queuesStores.getQueueOfQueuesOfInteractions().lock();
      LOG.info("QQI locked");

      queueOfInteractionsToProcess = queuesStores.getQueuesOfInteractionsStore().get(queueId);
      if (queueOfInteractionsToProcess == null) {
        return null;
      }
      queueOfInteractionsToProcess.lock();
      LOG.info("queue of interactions [{}] locked", queueOfInteractionsToProcess.getId());

      queueOfAgentsToProcess = queuesStores.getQueuesOfAgentsStore().get(queueId);
      if (queueOfAgentsToProcess == null) {
        return null;
      }
      queueOfAgentsToProcess.lock();
      LOG.info("queue of agents [{}] locked", queueOfAgentsToProcess.getId());

      boolean alreadyQueued = queuesStores.getQueueOfQueuesOfInteractions()
          .contains(queueOfInteractionsToProcess);

      if (queueOfInteractionsToProcess.size() < 1 && alreadyQueued) {
        queuesStores.getQueueOfQueuesOfInteractions().remove(queueOfInteractionsToProcess);
        LOG.info(
            "queue of interactions [{}] size is [{}], thus removed from queue of queues of interactions",
            queueId, queueOfInteractionsToProcess.size());
        return null;
      }

      if (queueOfAgentsToProcess.size() < 1 && alreadyQueued) {
        queuesStores.getQueueOfQueuesOfInteractions().remove(queueOfInteractionsToProcess);
        LOG.info(
            "queue of interactions [{}] size of queue of agents is [{}], thus removed from queue of queues of interactions",
            queueId, queueOfInteractionsToProcess.size());
        return null;
      }
      if (queueOfInteractionsToProcess.size() > 0 && queueOfAgentsToProcess.size() > 0
          && !alreadyQueued) {
        queuesStores.getQueueOfQueuesOfInteractions().sortedInsert(queueOfInteractionsToProcess);
        LOG.info(
            "queue [{}] is added as its size as queue of agents is [{}], and as queue of interactions is [{}]",
            queueId, queueOfAgentsToProcess, queueOfInteractionsToProcess);
      }
    } finally {
      queuesStores.getQueueOfQueuesOfInteractions().unlock();
      LOG.info("QQI unlocked");

      if (queueOfInteractionsToProcess != null) {
        queueOfInteractionsToProcess.unlock();
        LOG.info("queue of interactions [{}] unlocked", queueOfInteractionsToProcess.getId());
      }
      if (queueOfAgentsToProcess != null) {
        queueOfAgentsToProcess.unlock();
        LOG.info("queue of agents [{}] unlocked", queueOfAgentsToProcess.getId());
      }

      LOG.info("finished validation");
    }
    return null;
  }
}
