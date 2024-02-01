package interactions_center.agents_manager.functions.agent_state_fns;

import interactions_center.agents_manager.constants.AGENT_STATES;
import interactions_center.agents_manager.constants.WS_CMDS_REPLIES;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;
import interactions_center.agents_manager.stores.AgentsStore;
import interactions_center.agents_manager.stores.AgtsWsSessionsStore;
import interactions_center.agents_manager.stores.items.Agent;
import interactions_center.queuing_engine.functions.queues_of_agents.EnqAgtFn;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

@ApplicationScoped
public class SetReadyFn implements Consumer<WsMessage> {
    private final Logger LOG = LoggerFactory.getLogger(SetReadyFn.class);
    @Inject
    AgtsWsSessionsStore agtsWsSessionsStore;
    @Inject
    AgentsStore agentsStore;
    @Inject
    EnqAgtFn enqAgtFn;

    @Override
    public void accept(WsMessage message) {
        Agent agent = agentsStore.get(message.getRequestingAgentUuid());
        if (Objects.isNull(agent)) {
            LOG.warn("session [{}] : agtId [{}] : reqId [{}] : passed agent UUID doesn't exist in agents store",
                    message.getWsId(), message.getRequestingAgentUuid(), message.getReqId()
            );
            return;
        }
        LOG.info("session [{}] : agtId [{}] : reqId [{}] : processing request...", message.getWsId(), agent.getId(),
                message.getReqId());

        Session wsSession = agtsWsSessionsStore.getSessionByAgtUuid(agent.getId());
        if (Objects.isNull(wsSession)) {
            LOG.warn("session [{}] : agtId [{}] : reqId [{}] : agent doesn't have an active websocket session, seems like it was " +
                            "closed after receiving this request, neglecting request...",
                    message.getWsId(), agent.getId(), message.getReqId());
            return;
        }

        WsMessage response = new WsMessage();
        response.setReqId(message.getReqId());

        agent.lock();
        LOG.info("session [{}] : agtId [{}] : reqId [{}] : LOCKED agent ", message.getWsId(),
                agent.getId(), message.getReqId());

        try {
            if (Arrays.stream(AGENT_STATES.ALLOWED_TO_READY).noneMatch(allowedValue -> agent.getState() == allowedValue)) {
                LOG.info("session [{}] : agtId [{}] : reqId [{}] : agent is not allowed change state to ready because" +
                                " current state is [{}]",
                        message.getWsId(), agent.getId(), message.getReqId(), agent.getState());

                response.setError(true);
                response.setCode(WS_CMDS_REPLIES.NOT_ALLOWED_TO_READY_BECAUSE_OF_CURRENT_STATE);
                response.setMessage("Not Allowed to change state to Ready, your current state doesn't allow to");
                wsSession.getAsyncRemote().sendObject(response, (sendResult) -> {
                    if (sendResult.isOK()) {
                        LOG.info("session [{}] : agtId [{}] : reqId [{}] : response sent successfully",
                                message.getWsId(), agent.getId(), message.getReqId());
                    } else {
                        LOG.error("session [{}] : agtId [{}] : reqId [{}] : failed to send response",
                                message.getWsId(), agent.getId(), message.getReqId());
                    }
                });
                return;
            }

            enqAgtFn.accept(agent);
            agent.setState(AGENT_STATES.READY);
            agent.setLastStateChangedAt(message.getRequestedAt());

            response.setError(false);
            response.setCode(WS_CMDS_REPLIES.SUCCESS);
            response.setMessage("State changed to Ready");

            wsSession.getAsyncRemote().sendObject(response, (sendResult) -> {
                if (sendResult.isOK()) {
                    LOG.info("session [{}] : agtId [{}] : reqId [{}] : response sent successfully", message.getWsId(), agent.getId(), message.getReqId());
                } else {
                    LOG.error("session [{}] : agtId [{}] : reqId [{}] : failed to send response",
                            message.getWsId(), agent.getId(), message.getReqId());
                }
            });
        } finally {
            agent.unlock();
            LOG.info("session [{}] : agtId [{}] : reqId [{}] : UNLOCKED agent ", message.getWsId(),
                    agent.getId(), message.getReqId());
        }
    }
}
