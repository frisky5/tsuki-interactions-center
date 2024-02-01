package interactions_center.agents_manager.functions.agent_state_fns;

import interactions_center.agents_manager.constants.WS_CMDS_REPLIES;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;
import interactions_center.agents_manager.stores.AgentsStore;
import interactions_center.agents_manager.stores.AgtsWsSessionsStore;
import interactions_center.agents_manager.stores.items.Agent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

@ApplicationScoped
public class GetCurrentStateFn implements Consumer<WsMessage> {
    private final Logger LOG = LoggerFactory.getLogger(GetCurrentStateFn.class);

    @Inject
    AgtsWsSessionsStore agtsWsSessionsStore;
    @Inject
    AgentsStore agentsStore;

    @Override
    public void accept(WsMessage message) {
        Agent agent = agentsStore.get(message.getRequestingAgentUuid());

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

            response.setError(false);
            response.setCode(WS_CMDS_REPLIES.SUCCESS);
            response.setState(agent.getState());

            wsSession.getAsyncRemote().sendObject(response, (sendResult) -> {
                if (sendResult.isOK()) {
                    LOG.info("reqId [{}] : sent response to agent [{}] ",  message.getReqId(),agent.getId());
                } else {
                    LOG.warn("reqId [{}] : failed to send response", message.getReqId());
                }
            });
        } finally {
            agent.unlock();
            LOG.info("UNLOCKED agent [{}]", agent.getId());
        }
    }
}
