package solutions.tsuki.ic.queues.manager.functions.queuesOfAgents;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;

@ApplicationScoped
public class SortedInsertAgentIntoAssignedQueuesFunction implements Function<Agent, Void> {

  public final Logger LOG = LoggerFactory.getLogger("SortedInsertAgentIntoAssignedQueuesFunction");

  @Inject
  QueuesStores queuesStores;

  @Override
  public Void apply(Agent agent) {
    agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(assignedQueue -> {
      QueueOfAgents queueOfAgents = queuesStores.getQueuesOfAgentsStore().get(assignedQueue);
      queueOfAgents.lock();
      try {
        LOG.info("LOCKED queue of agents [{}]", queueOfAgents.getId());
        int insertedAt = queueOfAgents.sortedInsert(agent);
        LOG.info("agent [{}] is inserted into queue of agents [{}] at index [{}]",
            agent.getKeycloakUserUuid(), assignedQueue,
            insertedAt);
      } finally {
        queueOfAgents.unlock();
        LOG.info("UNLOCKED queue of agents [{}]", queueOfAgents.getId());
      }
    });
    return null;
  }
}
