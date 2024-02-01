package interactions_center.agents_manager.functions.agent_state_fns;

import interactions_center.agents_manager.constants.AGENT_STATES;
import interactions_center.agents_manager.constants.WS_CMDS_REPLIES;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;
import interactions_center.agents_manager.stores.AgentsStore;
import interactions_center.agents_manager.stores.AgtsWsSessionsStore;
import interactions_center.agents_manager.stores.items.Agent;
import interactions_center.queuing_engine.functions.queues_of_agents.DqAgtFn;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

@ApplicationScoped
public class SetNotReadyFn implements Consumer<WsMessage> {
    private final Logger LOG = LoggerFactory.getLogger(SetNotReadyFn.class);

    @Inject
    AgtsWsSessionsStore agtsWsSessionsStore;
    @Inject
    AgentsStore agentsStore;
    @Inject
    DqAgtFn dqAgtFn;

    @Override
    public void accept(WsMessage message) {
        Agent agent = agentsStore.get(message.getRequestingAgentUuid());
        if (Objects.isNull(agent)) {
            LOG.warn("passed agent UUID [{}] in WsMessage doesn't exist in agents store", message.getRequestingAgentUuid());
            return;
        }
        LOG.info("request [{}] from agent [{}]", message.getReqId(), agent.getId());

        Session wsSession = agtsWsSessionsStore.getSessionByAgtUuid(agent.getId());
        if (Objects.isNull(wsSession)) {
            LOG.warn("agent [{}] doesn't have a websocket session tp send response to, neglecting request...",
                    agent.getId());
            return;
        }

        WsMessage response = new WsMessage();
        response.setReqId(message.getReqId());

        try {
            agent.lock();
            LOG.info("LOCKED agent [{}]", agent.getId());

            if (Arrays.stream(AGENT_STATES.ALLOWED_TO_NOT_READY).noneMatch(allowedValue -> agent.getState() == allowedValue)) {
                LOG.info("agent [{}] is not allowed change state to not ready because current state is [{}]",
                        agent.getId(),
                        agent.getState());

                response.setError(true);
                response.setCode(WS_CMDS_REPLIES.NOT_ALLOWED_TO_NOT_READY_BECAUSE_OF_CURRENT_STATE);
                response.setMessage("Not Allowed to change state to Not Ready, your current state doesn't allow to");
                wsSession.getAsyncRemote().sendObject(response, (sendResult) -> {
                    if (sendResult.isOK()) {
                        LOG.info("sent response to agent [{}] on request id [{}]", agent.getId(), message.getReqId());
                    } else {
                        LOG.warn("failed to send response for request [{}] to agent [{}]", message.getReqId(),
                                agent.getId());
                    }
                });
                return;
            }

            dqAgtFn.accept(agent);
            agent.setState(AGENT_STATES.NOT_READY);
            agent.setLastStateChangedAt(message.getRequestedAt());

            response.setError(false);
            response.setCode(WS_CMDS_REPLIES.SUCCESS);
            response.setMessage("State changed to Not Ready");

            wsSession.getAsyncRemote().sendObject(response, (sendResult) -> {
                if (sendResult.isOK()) {
                    LOG.info("sent response to agent [{}] on request id [{}]", agent.getId(), message.getReqId());
                } else {
                    LOG.warn("failed to send response for request [{}] to agent [{}]", message.getReqId(),
                            agent.getId());
                }
            });
        } finally {
            agent.unlock();
            LOG.info("UNLOCKED agent [{}]", agent.getId());
        }
    }
}
