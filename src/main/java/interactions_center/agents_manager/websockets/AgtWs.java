package interactions_center.agents_manager.websockets;

import interactions_center.agents_manager.functions.AgentManagerCmdToSubCmdMapMap;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;
import interactions_center.agents_manager.stores.AgtsWsSessionsStore;
import interactions_center.agents_manager.websockets.decoders.JsonDecoder;
import interactions_center.agents_manager.websockets.encoders.JsonEncoder;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@ServerEndpoint(value = "/v1/agents_manager/websocket", decoders = JsonDecoder.class, encoders = JsonEncoder.class)
public class AgtWs {
    private final Logger LOG = LoggerFactory.getLogger(AgtWs.class);

    @Inject
    AgtsWsSessionsStore agtsWsSessionsStore;

    @Inject
    AgentManagerCmdToSubCmdMapMap agentManagerCmdToSubCmdMapMap;

    @OnOpen
    public void onOpen(Session session) {
        Infrastructure.getDefaultWorkerPool().execute(() -> {
            WsMessage message = new WsMessage();
            message.setWsId(session.getId());
            LOG.info("session [{}] : opened", session.getId());
            session.getAsyncRemote().sendObject(message, new SendHandler() {
                @Override
                public void onResult(SendResult result) {
                    LOG.info("session [{}] : sent session id to agent on session open for validation", session.getId());
                    agtsWsSessionsStore.putSession(session.getId(), session);
                }
            });
        });
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("session [{}] : closed", session.getId());
        agtsWsSessionsStore.removeSession(session.getId());
    }

    @OnMessage
    public void onMessage(WsMessage message, Session session) {
        Infrastructure.getDefaultWorkerPool().execute(() -> {
            UUID requestingAgentUuid = agtsWsSessionsStore.getAgtUuidBySessionId(session.getId());
            if (Objects.isNull(requestingAgentUuid)) {
                LOG.warn("session [{}] : received message while the session is not validated against an agent, " +
                                "terminating session",
                        session.getId());
                try {
                    session.close();
                    LOG.warn("session [{}] : session terminated", session.getId());
                } catch (IOException e) {
                    LOG.error("session [{}] : error terminating session, will be remove from tracking map anyway",
                            session.getId());
                } finally {
                    agtsWsSessionsStore.removeSession(session.getId());
                }
                return;
            }

            if (Objects.isNull(message.getCmd())) {
                LOG.warn("session [{}] : received message is missing cmd, neglecting message...", session.getId());
                return;
            }

            if (Objects.isNull(message.getSubCmd())) {
                LOG.warn("session [{}] : received message is missing subCmd, neglecting message...", session.getId());
                return;
            }

            if (Objects.isNull(message.getReqId())) {
                LOG.warn("session [{}] : received message is missing reqId, neglecting message...", session.getId());
                return;
            }


            message.setRequestingAgentUuid(requestingAgentUuid);
            message.setWsId(session.getId());

            LOG.info("session [{}] : agent [{}] : received request, reqId [{}]",
                    session.getId(), requestingAgentUuid, message.getReqId());
            agentManagerCmdToSubCmdMapMap.get(message.getCmd()).get(message.getSubCmd()).accept(message);
        });
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOG.error("error on session [{}]", session.getId(), throwable);
    }
}
