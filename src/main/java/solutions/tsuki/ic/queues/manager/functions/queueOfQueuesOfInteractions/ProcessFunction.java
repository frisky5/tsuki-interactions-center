package solutions.tsuki.ic.queues.manager.functions.queueOfQueuesOfInteractions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.agents.constants.AGENT_STATES;
import solutions.tsuki.constants.AGENT_WEBSOCKET_COMMANDS;
import solutions.tsuki.ic.interactions.constants.INTERACTION_STATE;
import solutions.tsuki.functions.agent.state.OfferingInteractionFunction;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.websocketsMessages.AgentWebsocketMessage;
import solutions.tsuki.quartzJobs.CheckInteractionOfferingToAgentAccepted;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;
import solutions.tsuki.ic.queues.manager.queues.interactions.QueueOfInteractions;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.queues.manager.queues.item.Interaction;


@ApplicationScoped
public class ProcessFunction implements Function<Void, Void> {

  public final Logger LOG = LoggerFactory.getLogger("QQI-ProcessFunction");
  @Inject
  QueuesStores queuesStores;

  @Inject
  OfferingInteractionFunction offeringInteractionFunction;

  @Inject
  ValidateByQueueFunction validateByQueueFunction;

  @Inject
  Scheduler quartz;

  @Override
  public Void apply(Void unused) {
    LOG.info("starting QQI processing");
    while (true) {
      QueueOfInteractions queueOfInteractionsToProcess = null;
      QueueOfAgents queueOfAgentsToProcess = null;
      Interaction interactionAtHead = null;
      Agent agentAtHead = null;

      queuesStores.getQueueOfQueuesOfInteractions().lock();
      LOG.info("LOCKED QQI");

      if (queuesStores.getQueueOfQueuesOfInteractions().size() == 0) {
        LOG.info("QQI is empty, no interactions to process");
        queuesStores.getQueueOfQueuesOfInteractions().unlock();
        LOG.info("UNLOCKED QQI");
        break;
      }

      queueOfInteractionsToProcess = queuesStores.getQueueOfQueuesOfInteractions().getHead();
      queueOfInteractionsToProcess.lock();
      LOG.info("LOCKED queue of interactions [{}]", queueOfInteractionsToProcess.getId());

      queueOfAgentsToProcess = queuesStores.getQueuesOfAgentsStore()
          .get(queueOfInteractionsToProcess.getId());
      queueOfAgentsToProcess.lock();
      LOG.info("LOCKED queue of agents to process [{}]",
          queueOfInteractionsToProcess.getId());

      interactionAtHead = queueOfInteractionsToProcess.getHead();
      interactionAtHead.lock();
      LOG.info("LOCKED interaction [{}]", interactionAtHead.getId());

      agentAtHead = queuesStores.getQueuesOfAgentsStore()
          .get(queueOfInteractionsToProcess.getId())
          .getHead();
      agentAtHead.lock();
      LOG.info("LOCKED agent [{}]", agentAtHead.getKeycloakUserUuid());

      LOG.info("interaction [{}] at head of queue [{}] must be offered to agent[{}]",
          interactionAtHead.getId(), queueOfInteractionsToProcess.getId(),
          agentAtHead.getKeycloakUserUuid());

      LOG.info("requesting to change agent [{}] state to offering an interaction",
          agentAtHead.getKeycloakUserUuid());
      AgentRequest agentRequest = new AgentRequest();
      agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));
      agentRequest.setId(agentAtHead.getKeycloakUserUuid());
      agentRequest.setState(AGENT_STATES.OFFERING_AN_INTERACTION);

      int result = offeringInteractionFunction.apply(agentRequest);
      if (result != 0) {
        LOG.error(
            "result of changing agent [{}] state to offering an interaction is [{}], agent state might have been changed from another thread",
            agentAtHead.getKeycloakUserUuid(), result);
        continue;
      }

      boolean isRemoved = queueOfInteractionsToProcess.remove(interactionAtHead);
      if (!isRemoved) {
        LOG.error(
            "interaction [{}] failed to be removed from queue [{}], this might result in getting stuck in an infinite loop",
            interactionAtHead.getId(),
            interactionAtHead.getQueueId());
        continue;
      }

      interactionAtHead.setState(INTERACTION_STATE.OFFERING_TO_AGENT);
      LOG.info("interaction [{}] state changed to offering to an agent",
          interactionAtHead.getId());

      agentAtHead.assignInteraction(interactionAtHead);
      LOG.info("agent [{}] is assigned interaction [{}]", agentAtHead.getKeycloakUserUuid(),
          interactionAtHead.getId());

      queueOfInteractionsToProcess.getTimeMeasurement()
          .setLastOfferedFrom(LocalDateTime.now(ZoneId.of("UTC")));
      LOG.info("updated queue [{}] last offered at to [{}]",
          queueOfInteractionsToProcess.getId(),
          queueOfInteractionsToProcess.getTimeMeasurement().getLastOfferedFrom());

      AgentWebsocketMessage wsMessage = new AgentWebsocketMessage();
      wsMessage.setIsResponse(false);
      wsMessage.setCommand(AGENT_WEBSOCKET_COMMANDS.CHANGE_STATE);
      wsMessage.setState(AGENT_STATES.OFFERING_AN_INTERACTION);
      wsMessage.setInteractionId(interactionAtHead.getId());

      LOG.info(
          "sending state change to offering an interaction to agent [{}] on the websocket for the interaction [{}]",
          agentAtHead.getKeycloakUserUuid(), interactionAtHead.getId());

      queuesStores.getAgentsWebsocketSessionsStore()
          .sendMessage(agentAtHead.getKeycloakUserUuid(), wsMessage);
      LOG.info(
          "scheduling quartz job for interaction [{}] and agent [{}] to run after 10 seconds to check if agent accepted the interaction or should return it to queue",
          interactionAtHead.getId(), agentAtHead.getKeycloakUserUuid());

      JobDataMap jobData = new JobDataMap();
      jobData.put("agent", agentAtHead);
      jobData.put("interaction", interactionAtHead);
      SimpleTrigger trigger =
          (SimpleTrigger) TriggerBuilder.newTrigger()
              .startAt(Date.from(Instant.now().plusSeconds(10))).build();
      JobDetail jobDetail =
          JobBuilder.newJob().ofType(CheckInteractionOfferingToAgentAccepted.class)
              .usingJobData(jobData).build();
      try {
        quartz.scheduleJob(jobDetail, trigger);
      } catch (SchedulerException e) {
        LOG.error("failed to schedule check for answering of interaction [{}] for agent [{}]",
            interactionAtHead.getId(), agentAtHead.getKeycloakUserUuid());
      }

      if (agentAtHead != null) {
        agentAtHead.unlock();
        LOG.info("agent at head [{}] unlocked", agentAtHead.getKeycloakUserUuid());
      }
      if (interactionAtHead != null) {
        interactionAtHead.unlock();
        LOG.info("interaction at head [{}] unlocked", interactionAtHead.getId());
      }
      if (queueOfAgentsToProcess != null) {
        queueOfAgentsToProcess.unlock();
        LOG.info("queue of agents to process at head [{}] unlocked",
            queueOfAgentsToProcess.getId());
      }
      if (queueOfInteractionsToProcess != null) {
        queueOfInteractionsToProcess.unlock();
        LOG.info("queue of interactions to process at head [{}] unlocked",
            queueOfInteractionsToProcess.getId());
      }
      queuesStores.getQueueOfQueuesOfInteractions().unlock();
      LOG.info("QQI unlocked");
      if (queueOfInteractionsToProcess != null) {
        LOG.info("calling QQI validate by queue");
        validateByQueueFunction.apply(queueOfInteractionsToProcess.getId());
      }
    }
    LOG.info("finished processing");
    return null;
  }
}
