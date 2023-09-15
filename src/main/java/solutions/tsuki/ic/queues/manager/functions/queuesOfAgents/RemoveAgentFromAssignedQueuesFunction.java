package solutions.tsuki.ic.queues.manager.functions.queuesOfAgents;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;

@ApplicationScoped
public class RemoveAgentFromAssignedQueuesFunction implements Function<Agent, Void> {

  public final Logger LOG = LoggerFactory.getLogger("RemoveAgentFromAssignedQueuesFunction");

  @Inject
  QueuesStores queuesStores;

  @Override
  public Void apply(Agent agent) {
    agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(assignedQueue -> {
      QueueOfAgents queueOfAgents = queuesStores.getQueuesOfAgentsStore().get(assignedQueue);
      queueOfAgents.lock();
      try {
        LOG.info("LOCKED queue of agents [{}]", queueOfAgents.getId());
        queueOfAgents.remove(agent);
      } finally {
        queueOfAgents.unlock();
        LOG.info("UNLOCKED queue of agents [{}]", queueOfAgents.getId());
      }
    });
    return null;
  }
}
