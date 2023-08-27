package solutions.tsuki.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.AuthzClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.json.websocketsMessages.AgentWebsocketMessage;
import solutions.tsuki.stores.AgentsWebsocketSessionsStore;

import java.io.IOException;

import static solutions.tsuki.constants.WEBSOCKETS_COMMANDS.AGENT_REQUEST_AUTH;

@ServerEndpoint(value = "/v1/ws/agent")
@ApplicationScoped
public class AgentWebsocket {
    public Logger logger = LoggerFactory.getLogger("AgentWebsocket");

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AuthzClient authzClient;

    @Inject
    AgentsWebsocketSessionsStore agentsWebsocketSessionsStore;


    @OnOpen
    public void onOpen(Session session) {


        agentsWebsocketSessionsStore.putSession(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        agentsWebsocketSessionsStore.invalidateSession(session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        AgentWebsocketMessage messageObj = null;
        try {
            messageObj = objectMapper.readValue(message, AgentWebsocketMessage.class);
        } catch (JsonProcessingException e) {
            logger.error("failed to JSON parse message on session [{}], bad received message [{}]", session.getId(), message);
            return;
        }
        if (messageObj.getCommand() == null) {
            logger.warn("received a message without a command on session [{}], neglecting message.", session.getId());
            return;
        }
        if (messageObj.getCommand() == AGENT_REQUEST_AUTH) {
            try {
                authzClient.authorization(messageObj.getToken()).authorize();
                agentsWebsocketSessionsStore.addValidatedSession(session.getId());
                logger.info("session [{}] is validated with the provided token", session.getId());
                AgentWebsocketMessage response = new AgentWebsocketMessage();
                response.setIsResponse(true);
                response.setError(false);
                response.setSessionId(session.getId());
                String responseStr;
                try {
                    responseStr = objectMapper.writeValueAsString(response);
                    session.getAsyncRemote().sendText(responseStr);
                } catch (JsonProcessingException e) {
                    logger.error("failed to parse JSON response as string", e);
                    session.getAsyncRemote().sendText("{\"error\":true}");
                }

            } catch (AuthorizationDeniedException e) {
                logger.error("failed to authenticate token on session [{}], closing session!", session.getId());
                try {
                    session.close();
                } catch (IOException ex) {
                    logger.error("failed to close session [{}]", session.getId());
                }
            } catch (Exception e) {
                logger.error("exception occurred", e);
            }
        }

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("error on session [{}]", session.getId());
    }
}
