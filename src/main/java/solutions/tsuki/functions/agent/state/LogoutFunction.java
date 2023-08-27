package solutions.tsuki.functions.agent.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.functions.database.InsertAgentStateEventFunction;
import solutions.tsuki.functions.queue.agents.RemoveAgentFromAssignedQueuesFunction;
import solutions.tsuki.functions.queue.queueOfInteractions.ValidateByAgentFunction;
import solutions.tsuki.json.requests.AgentRequest;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.stores.StoresDTO;

@ApplicationScoped
public class LogoutFunction implements Function<AgentRequest, Integer> {

  public final Logger LOG = LoggerFactory.getLogger("LogoutFunction");

  @Inject
  StoresDTO storesDTO;

  @Inject
  ExecutorsFactory executorsFactory;

  @Inject
  InsertAgentStateEventFunction insertAgentStateEventFunction;

  @Inject
  ValidateByAgentFunction validateByAgentFunction;

  @Inject
  RemoveAgentFromAssignedQueuesFunction removeAgentFromAssignedQueuesFunction;

  @Override
  public Integer apply(AgentRequest request) {
    Agent agent = storesDTO.getAgentsStore().get(request.getId());
    if (agent == null) {
      return -2;
    }

    boolean allowed = Arrays.stream(AGENT_STATES.ALLOWED_TO_LOGOUT)
        .anyMatch(state -> state == agent.getState());

    if (!allowed) {
      return -1;
    }

    agent.getTimeMeasurements().setLoggedOutAt(request.getRequestAt());
    agent.setState(AGENT_STATES.LOGGED_OUT);
    request.setState(AGENT_STATES.LOGGED_OUT);

    removeAgentFromAssignedQueuesFunction.apply(agent);

    executorsFactory.getDatabaseMultiThreadExecutor().execute(() -> {
      insertAgentStateEventFunction.apply(request);
    });

    executorsFactory.getQqiSingleThreadExecutor().execute(() -> {
      validateByAgentFunction.apply(agent);
    });

    LOG.info("agent [{}] state changed to logged out, logged out time is [{}]", agent.getId(),
        agent.getTimeMeasurements().getLoggedOutAt());
    return 0;

  }
}
