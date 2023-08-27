package solutions.tsuki.quartzJobs;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queueItems.Interaction;

public class CheckInteractionOfferingToAgentAccepted implements Job {
    public final Logger logger = LoggerFactory.getLogger("CheckInteractionOfferingToAgentAccepted");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        try {
            Interaction interaction = (Interaction) jobDataMap.get("interaction");
            Agent agent = (Agent) jobDataMap.get("agent");
            if (interaction == null || agent == null) {
                logger.error("something is null");
            }
            logger.info("interaction [{}] state is [{}] and mapped with agent [{}]", interaction
                            .getId(),
                    interaction.getState(),
                    agent.getId());
        } catch (Exception e) {
            logger.error("failed ", e);
        }

    }
}

