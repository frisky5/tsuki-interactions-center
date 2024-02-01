package interactions_center.agents_manager.functions.agent_state_fns;

import interactions_center.agents_manager.constants.WS_CMDS_REPLIES;
import interactions_center.agents_manager.stores.AgentsStore;
import interactions_center.agents_manager.stores.AgtsWsSessionsStore;
import interactions_center.agents_manager.stores.items.Agent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;
import interactions_center.queuing_engine.functions.queues_of_agents.DqAgtFn;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import static interactions_center.agents_manager.constants.AGENT_STATES.ALLOWED_TO_LOGOUT;
import static interactions_center.agents_manager.constants.AGENT_STATES.LOGOUT;

@ApplicationScoped
public class SetLogoutFn implements Consumer<WsMessage> {
    private final Logger LOG = LoggerFactory.getLogger(SetLogoutFn.class);

    @Inject
    AgentsStore agentsStore;
    @Inject
    DqAgtFn dqAgtFn;
    @Inject
    AgtsWsSessionsStore agtsWsSessionsStore;

    @Override
    public void accept(WsMessage message) {
        Agent agent = agentsStore.get(message.getRequestingAgentUuid());
        if (Objects.isNull(agent)) {
            LOG.warn("passed agent UUID [{}] in WsMessage doesn't exist in agents store", message.getRequestingAgentUuid());
            return;
        }
        LOG.info("request [{}] from agent [{}]", message.getReqId(), agent.getId());

        try {
            agent.lock();
            LOG.info("LOCKED agent [{}]", agent.getId());

            Session wsSession = agtsWsSessionsStore.getSessionByAgtUuid(agent.getId());
            if (Objects.isNull(wsSession)) {
                LOG.warn("agent [{}] doesn't have a websocket session tp send response to, neglecting request...",
                        agent.getId());
                return;
            }

            WsMessage response = new WsMessage();
            response.setReqId(message.getReqId());

            if (Arrays.stream(ALLOWED_TO_LOGOUT).noneMatch(allowedValue -> agent.getState() == allowedValue)) {
                LOG.info("agent [{}] is not allowed to logout because current state is [{}]", agent.getId(),
                        agent.getState());
                response.setError(true);
                response.setCode(WS_CMDS_REPLIES.NOT_ALLOWED_TO_LOGOUT_BECAUSE_OF_CURRENT_STATE);
                response.setMessage("Not Allowed to Logout, your current state doesn't allow to");
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
            agent.setState(LOGOUT);
            agent.setLastStateChangedAt(message.getRequestedAt());

            response.setError(false);
            response.setCode(WS_CMDS_REPLIES.SUCCESS);
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
