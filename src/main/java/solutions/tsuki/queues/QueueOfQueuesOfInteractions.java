package solutions.tsuki.queues;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.constants.INTERACTION_STATE;
import solutions.tsuki.functions.agent.GoOfferingInteractionFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.responses.AgentResponse;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.queueItems.comparators.queue.interactionsQueue.TypeOne;
import solutions.tsuki.stores.QueuesOfAgentsStore;
import solutions.tsuki.stores.QueuesOfInteractionsStore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@ApplicationScoped
public class QueueOfQueuesOfInteractions {

    public final Logger logger = LoggerFactory.getLogger("QueueOfQueuesOfInteractions");

    @Inject
    public QueuesOfAgentsStore queuesOfAgentsStore;

    @Inject
    public QueuesOfInteractionsStore queuesOfInteractionsStore;

    @Inject
    GoOfferingInteractionFunction goOfferingInteractionFunction;

    public ArrayList<QueueOfInteractions> queue = new ArrayList<>(10);
    public Comparator<QueueOfInteractions> comparator = new TypeOne();


    public QueueOfInteractions getHead() {
        return queue.size() > 0 ? queue.get(0) : null;
    }

    public void validate(Integer queueId) {
        boolean isQueueContains = contains(queueId);
        int sizeOfAgentsQueue = queuesOfAgentsStore.sizeOfQueue(queueId);
        int sizeOfInteractionsQueue = queuesOfInteractionsStore.sizeOfQueue(queueId);
        if (sizeOfInteractionsQueue < 1 && isQueueContains) {
            queue.remove(queuesOfInteractionsStore.get(queueId));
            logger.info("queues [{}] is removed from queue due to its size as queue of interactions of [{}]",
                    queueId, sizeOfInteractionsQueue);
            return;
        }
        if (sizeOfAgentsQueue < 1 && isQueueContains) {
            queue.remove(queuesOfInteractionsStore.get(queueId));
            logger.info("queues [{}] is removed from queue due to its size as queue of agents of [{}]",
                    queueId, sizeOfAgentsQueue);
            return;
        }
        if (sizeOfAgentsQueue > 0 && sizeOfInteractionsQueue > 0 && !isQueueContains) {
            sortedInsert(queuesOfInteractionsStore.get(queueId));
            logger.info("queue [{}] is added as its size as queue of agents is [{}], and as queue of interactions" +
                    " is [{}]", queueId, sizeOfAgentsQueue, sizeOfInteractionsQueue);
            return;
        }
    }

    public void validateByAgent(Agent agent) {
        agent.getAssignedInteractionsQueues().keySet().iterator().forEachRemaining(assignedQueue -> {
            boolean isQueueContains = contains(assignedQueue);
            int sizeOfAgentsQueue = queuesOfAgentsStore.sizeOfQueue(assignedQueue);
            int sizeOfInteractionsQueue = queuesOfInteractionsStore.sizeOfQueue(assignedQueue);

            if (sizeOfInteractionsQueue < 1 && isQueueContains) {
                queue.remove(queuesOfInteractionsStore.get(assignedQueue));
                logger.info("queues [{}] is removed from queue due to its size as queue of interactions of [{}]",
                        assignedQueue, sizeOfInteractionsQueue);
                return;
            }
            if (sizeOfAgentsQueue < 1 && isQueueContains) {
                queue.remove(queuesOfInteractionsStore.get(assignedQueue));
                logger.info("queues [{}] is removed from queue due to its size as queue of agents of [{}]",
                        assignedQueue, sizeOfAgentsQueue);
                return;
            }
            if (sizeOfAgentsQueue > 0 && sizeOfInteractionsQueue > 0 && !isQueueContains) {
                sortedInsert(queuesOfInteractionsStore.get(assignedQueue));
                logger.info("queue [{}] is added as its size as queue of agents is [{}], and as queue of interactions" +
                        " is [{}]", assignedQueue, sizeOfAgentsQueue, sizeOfInteractionsQueue);
                return;
            }
        });
    }

    public void process() {
        while (queue.size() > 0) {
            QueueOfInteractions queueOfInteractionsAtHead = getHead();
            Interaction interactionAtHead = queueOfInteractionsAtHead.getHead();
            Agent agentAtHead = queuesOfAgentsStore.get(queueOfInteractionsAtHead.getId()).getHead();
            logger.info("interaction [{}] at head of queue [{}] must be offered to agent[{}]",
                    interactionAtHead.getId(), queueOfInteractionsAtHead.getId(), agentAtHead.getId());

            AgentRequest agentRequest = new AgentRequest();
            agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));
            agentRequest.setId(agentAtHead.getId());
            logger.info("requesting to change agent [{}] state to offering interaction", agentAtHead.getId());
            AgentResponse response = goOfferingInteractionFunction.apply(agentRequest);
            if (response.getError()) {
                logger.error("failed to change agent state to offering interaction, cannot proceed with interaction " +
                        "offering to the agent, might get stuck in an infinite loop");
                continue;
            }

            boolean isRemoved = queuesOfInteractionsStore.dequeue(interactionAtHead);
            if (!isRemoved) {
                logger.error("interaction [{}] failed to be removed from queue [{}], this might result in getting " +
                        "stuck in an infinite loop", interactionAtHead.getId(), interactionAtHead.getQueueId());
                continue;
            }
            agentAtHead.assignInteraction(interactionAtHead);
            logger.info("agent [{}] is assigned interaction [{}]", agentAtHead.getId(), interactionAtHead.getId());
            queueOfInteractionsAtHead.getTimeMeasurement().setLastOfferedFrom(LocalDateTime.now(ZoneId.of("UTC")));
            logger.info("updated queue [{}] last offered at to [{}]", queueOfInteractionsAtHead.getId(),
                    queueOfInteractionsAtHead.getTimeMeasurement().getLastOfferedFrom());
            logger.info("changing interaction [{}] state to offering to agent", interactionAtHead.getId());
            interactionAtHead.setState(INTERACTION_STATE.OFFERING_TO_AGENT);

        }
    }

    public boolean contains(Integer queueId) {
        return queue.contains(queuesOfInteractionsStore.get(queueId));
    }

    public boolean contains(QueueOfInteractions queue) {
        return this.queue.contains(queue);
    }

    public int sortedInsert(QueueOfInteractions queueToInsert) {
        if (!queuesOfInteractionsStore.hasQueue(queueToInsert.getId())) {
            return -2;
        }
        if (queue.contains(queueToInsert)) {
            return -1;
        }
        if (queue.size() == 0) {
            queue.add(queueToInsert);
            return 0;
        } else {
            int insertIndex =
                    -1 * (Collections.binarySearch(queue, queueToInsert,
                            comparator)
                            + 1);
            queue.add(insertIndex, queueToInsert);
            return insertIndex;
        }
    }

}
