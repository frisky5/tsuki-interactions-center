package solutions.tsuki.functions.queue.queueOfInteractions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queue.agents.QueueOfAgents;
import solutions.tsuki.queue.interactions.QueueOfInteractions;
import solutions.tsuki.stores.StoresDTO;


@ApplicationScoped
public class ValidateByAgentFunction implements Function<Agent, Void> {

  public final Logger LOG = LoggerFactory.getLogger("QQI-ValidateByAgentFunction");

  @Inject
  StoresDTO storesDTO;

  @Override
  public Void apply(Agent agent) {
    LOG.info("validation started");

    try {
      storesDTO.getQueueOfQueuesOfInteractions().lock();
      LOG.info("QQI locked");

      agent.lock();
      LOG.info("agent [{}] locked", agent.getId());

      agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(queueId -> {
        QueueOfInteractions queueOfInteractionsToProcess = null;
        QueueOfAgents queueOfAgentsToProcess = null;

        queueOfInteractionsToProcess = storesDTO.getQueuesOfInteractionsStore().get(queueId);
        if (queueOfInteractionsToProcess == null) {
          return;
        }

        queueOfAgentsToProcess = storesDTO.getQueuesOfAgentsStore().get(queueId);
        if (queueOfAgentsToProcess == null) {
          return;
        }

        try {
          queueOfInteractionsToProcess.lock();
          LOG.info("queue of interactions [{}] locked", queueOfInteractionsToProcess.getId());

          queueOfAgentsToProcess.lock();
          LOG.info("queue of agents [{}] locked", queueOfAgentsToProcess.getId());

          LOG.info("validating queue [{}]", queueOfInteractionsToProcess.getId());
          boolean isAlreadyQueued = storesDTO.getQueueOfQueuesOfInteractions()
              .contains(queueOfInteractionsToProcess);

          if (queueOfInteractionsToProcess.size() < 1 && isAlreadyQueued) {
            storesDTO.getQueueOfQueuesOfInteractions().remove(queueOfInteractionsToProcess);
            LOG.info(
                "queue [{}] is removed from queue of interactions queues due to its size of queue of interactions of [{}]",
                queueOfInteractionsToProcess, queueOfInteractionsToProcess.size());
            return;
          }
          if (queueOfAgentsToProcess.size() < 1 && isAlreadyQueued) {
            storesDTO.getQueueOfQueuesOfInteractions().remove(queueOfInteractionsToProcess);
            LOG.info("queues [{}] is removed from queue due to its size as queue of agents of [{}]",
                queueOfInteractionsToProcess, queueOfAgentsToProcess.size());
            return;
          }
          if (queueOfInteractionsToProcess.size() > 0 && queueOfAgentsToProcess.size() > 0
              && !isAlreadyQueued) {
            storesDTO.getQueueOfQueuesOfInteractions().sortedInsert(queueOfInteractionsToProcess);
            LOG.info(
                "queue [{}] is added as its size as queue of agents is [{}], and as queue of interactions is [{}]",
                queueOfInteractionsToProcess, queueOfAgentsToProcess.size(),
                queueOfInteractionsToProcess.size());
            return;
          }
          LOG.info("no changes wre made for queue [{}]", queueOfInteractionsToProcess.getId());
        } finally {
          queueOfAgentsToProcess.unlock();
          LOG.info("queue of agents [{}] unlocked", queueOfAgentsToProcess.getId());

          queueOfInteractionsToProcess.unlock();
          LOG.info("queue of interactions [{}] unlocked", queueOfInteractionsToProcess.getId());
        }

      });
    } finally {
      agent.unlock();
      LOG.info("agent [{}] unlocked", agent.getId());

      storesDTO.getQueueOfQueuesOfInteractions().unlock();
      LOG.info("QQI unlocked");

      LOG.info("validation finished");
    }
    return null;
  }
}
