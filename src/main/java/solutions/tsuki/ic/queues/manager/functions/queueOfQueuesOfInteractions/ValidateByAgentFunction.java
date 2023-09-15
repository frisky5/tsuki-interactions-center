package solutions.tsuki.ic.queues.manager.functions.queueOfQueuesOfInteractions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.queues.manager.queues.interactions.QueueOfInteractions;


@ApplicationScoped
public class ValidateByAgentFunction implements Function<Agent, Void> {

  public final Logger LOG = LoggerFactory.getLogger("QQI-ValidateByAgentFunction");

  @Inject
  QueuesStores queuesStores;

  @Override
  public Void apply(Agent agent) {
    LOG.info("validation started");

    try {
      queuesStores.getQueueOfQueuesOfInteractions().lock();
      LOG.info("QQI locked");

      agent.lock();
      LOG.info("agent [{}] locked", agent.getKeycloakUserUuid());

      agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(queueId -> {
        QueueOfInteractions queueOfInteractionsToProcess = null;
        QueueOfAgents queueOfAgentsToProcess = null;

        queueOfInteractionsToProcess = queuesStores.getQueuesOfInteractionsStore().get(queueId);
        if (queueOfInteractionsToProcess == null) {
          return;
        }

        queueOfAgentsToProcess = queuesStores.getQueuesOfAgentsStore().get(queueId);
        if (queueOfAgentsToProcess == null) {
          return;
        }

        try {
          queueOfInteractionsToProcess.lock();
          LOG.info("queue of interactions [{}] locked", queueOfInteractionsToProcess.getId());

          queueOfAgentsToProcess.lock();
          LOG.info("queue of agents [{}] locked", queueOfAgentsToProcess.getId());

          LOG.info("validating queue [{}]", queueOfInteractionsToProcess.getId());
          boolean isAlreadyQueued = queuesStores.getQueueOfQueuesOfInteractions()
              .contains(queueOfInteractionsToProcess);

          if (queueOfInteractionsToProcess.size() < 1 && isAlreadyQueued) {
            queuesStores.getQueueOfQueuesOfInteractions().remove(queueOfInteractionsToProcess);
            LOG.info(
                "queue [{}] is removed from queue of interactions queues due to its size of queue of interactions of [{}]",
                queueOfInteractionsToProcess, queueOfInteractionsToProcess.size());
            return;
          }
          if (queueOfAgentsToProcess.size() < 1 && isAlreadyQueued) {
            queuesStores.getQueueOfQueuesOfInteractions().remove(queueOfInteractionsToProcess);
            LOG.info("queues [{}] is removed from queue due to its size as queue of agents of [{}]",
                queueOfInteractionsToProcess, queueOfAgentsToProcess.size());
            return;
          }
          if (queueOfInteractionsToProcess.size() > 0 && queueOfAgentsToProcess.size() > 0
              && !isAlreadyQueued) {
            queuesStores.getQueueOfQueuesOfInteractions().sortedInsert(queueOfInteractionsToProcess);
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
      LOG.info("agent [{}] unlocked", agent.getKeycloakUserUuid());

      queuesStores.getQueueOfQueuesOfInteractions().unlock();
      LOG.info("QQI unlocked");

      LOG.info("validation finished");
    }
    return null;
  }
}
