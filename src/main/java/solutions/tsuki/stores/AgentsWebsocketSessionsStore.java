package solutions.tsuki.stores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.json.websocketsMessages.AgentWebsocketMessage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AgentsWebsocketSessionsStore {
    @Inject
    ObjectMapper objectMapper;

    public final Logger logger = LoggerFactory.getLogger("AgentsWebsocketSessionsStore");
    public ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> usersToSessions = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> sessionToUser = new ConcurrentHashMap<>();
    public Set<String> validatedSessions = new HashSet<>();

    public void putSession(String id, Session session) {
        sessions.put(id, session);
    }

    public void removeSession(String id) {
        sessions.remove(id);
    }

    public void putUserToSessionMap(String userId, String sessionId) {
        usersToSessions.put(userId, sessionId);
        sessionToUser.put(sessionId, userId);
    }

    public void addValidatedSession(String id) {
        validatedSessions.add(id);
    }

    public boolean sessionExist(String id) {
        return sessions.containsKey(id);
    }

    public boolean isSessionValidated(String id) {
        return validatedSessions.contains(id);
    }

    public Session getSessionBySessionId(String sessionId) {
        return sessions.get(sessionId);
    }

    public String getSessionIdByUserId(String userId) {
        return usersToSessions.get(userId);
    }

    public void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
        validatedSessions.remove(sessionId);
        if (sessionToUser.containsKey(sessionId)) {
            usersToSessions.remove(sessionToUser.get(sessionId));
            sessionToUser.remove(sessionId);
        }
        logger.info("invalidated session id [{}]", sessionId);
    }

    public void sendMessage(String agentId, AgentWebsocketMessage message) {
        try {
            sessions.get(usersToSessions.get(agentId)).getAsyncRemote().sendText(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("failed parsing message object as string", e);
        }
    }
}
