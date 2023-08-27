package solutions.tsuki.functions.queue.queueOfInteractions;

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
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.constants.AGENT_WEBSOCKET_COMMANDS;
import solutions.tsuki.constants.INTERACTION_STATE;
import solutions.tsuki.functions.agent.state.OfferingInteractionFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.json.websocketsMessages.AgentWebsocketMessage;
import solutions.tsuki.quartzJobs.CheckInteractionOfferingToAgentAccepted;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.queue.agents.QueueOfAgents;
import solutions.tsuki.queue.interactions.QueueOfInteractions;
import solutions.tsuki.stores.StoresDTO;


@ApplicationScoped
public class ProcessFunction implements Function<Void, Void> {

  public final Logger LOG = LoggerFactory.getLogger("QQI-ProcessFunction");
  @Inject
  StoresDTO storesDTO;

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
      try {
        storesDTO.getQueueOfQueuesOfInteractions().lock();
        LOG.info("QQI locked");

        if (storesDTO.getQueueOfQueuesOfInteractions().size() == 0) {
          LOG.info("QQI is empty, no interactions to process");
          break;
        }

        queueOfInteractionsToProcess = storesDTO.getQueueOfQueuesOfInteractions().getHead();
        if (queueOfInteractionsToProcess == null) {
          LOG.warn("queue of interactions to process (head of QQI) is null, breaking the loop");
          break;
        }
        queueOfInteractionsToProcess.lock();
        LOG.info("queue of interactions [{}] locked", queueOfInteractionsToProcess.getId());

        queueOfAgentsToProcess = storesDTO.getQueuesOfAgentsStore()
            .get(queueOfInteractionsToProcess.getId());
        if (queueOfAgentsToProcess == null) {
          LOG.warn("queue of agents to process is null");
          continue;
        }
        queueOfAgentsToProcess.lock();
        LOG.info("queue of agents to process [{}] locked",
            queueOfInteractionsToProcess.getId());

        interactionAtHead = queueOfInteractionsToProcess.getHead();
        if (interactionAtHead == null) {
          LOG.warn("interaction at head is null");
          continue;
        }
        interactionAtHead.lock();
        LOG.info("interaction [{}] locked", interactionAtHead.getId());

        agentAtHead = storesDTO.getQueuesOfAgentsStore()
            .get(queueOfInteractionsToProcess.getId())
            .getHead();
        if (agentAtHead == null) {
          LOG.warn("agent at head is null");
          continue;
        }
        agentAtHead.lock();
        LOG.info("agent [{}] locked", agentAtHead.getId());

        LOG.info("interaction [{}] at head of queue [{}] must be offered to agent[{}]",
            interactionAtHead.getId(), queueOfInteractionsToProcess.getId(),
            agentAtHead.getId());

        LOG.info("requesting to change agent [{}] state to offering an interaction",
            agentAtHead.getId());
        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setRequestAt(LocalDateTime.now(ZoneId.of("UTC")));
        agentRequest.setId(agentAtHead.getId());
        agentRequest.setState(AGENT_STATES.OFFERING_AN_INTERACTION);

        int result = offeringInteractionFunction.apply(agentRequest);
        if (result != 0) {
          LOG.error(
              "result of changing agent [{}] state to offering an interaction is [{}], agent state might have been changed from another thread",
              agentAtHead.getId(), result);
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
        LOG.info("agent [{}] is assigned interaction [{}]", agentAtHead.getId(),
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
            agentAtHead.getId(), interactionAtHead.getId());

        storesDTO.getAgentsWebsocketSessionsStore().sendMessage(agentAtHead.getId(), wsMessage);
        LOG.info(
            "scheduling quartz job for interaction [{}] and agent [{}] to run after 10 seconds to check if agent accepted the interaction or should return it to queue",
            interactionAtHead.getId(), agentAtHead.getId());

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
              interactionAtHead.getId(), agentAtHead.getId());
        }

      } finally {
        if (agentAtHead != null) {
          agentAtHead.unlock();
          LOG.info("agent at head [{}] unlocked", agentAtHead.getId());
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
        storesDTO.getQueueOfQueuesOfInteractions().unlock();
        LOG.info("QQI unlocked");
        if (queueOfInteractionsToProcess != null) {
          LOG.info("calling QQI validate by queue");
          validateByQueueFunction.apply(queueOfInteractionsToProcess.getId());
        }

      }
    }
    LOG.info("finished processing");
    return null;
  }
}
