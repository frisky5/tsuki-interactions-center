package interactions_center.agents_manager.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AgtsWsSessionsStore {

    public final Logger logger = LoggerFactory.getLogger(AgtsWsSessionsStore.class);
    public Map<String, Session> sessionIdToSession = new ConcurrentHashMap<>();
    public Map<UUID, Session> agtUuidToSession = new ConcurrentHashMap<>();
    public Map<String, UUID> sessionIdToAgtUuid = new ConcurrentHashMap<>();

    public void putSession(String id, Session session) {
        sessionIdToSession.put(id, session);
    }

    public void putAgentUuidToSession(UUID agtUuid, Session session) {
        agtUuidToSession.put(agtUuid, session);
    }

    public void removeSession(String sessionId) {
        UUID mappedAgent = sessionIdToAgtUuid.get(sessionId);
        if (Objects.nonNull(mappedAgent)) {
            agtUuidToSession.remove(mappedAgent);
            sessionIdToAgtUuid.remove(sessionId);
        }
        sessionIdToSession.remove(sessionId);
    }

    public short mapAgtUuidToSession(UUID agtUuid, String sessionId) {
        if(Objects.isNull(agtUuid) || Objects.isNull(sessionId))
            return -2;
        if (Objects.nonNull(sessionIdToSession.get(sessionId))) {
            agtUuidToSession.put(agtUuid, sessionIdToSession.get(sessionId));
            sessionIdToAgtUuid.put(sessionIdToSession.get(sessionId).getId(), agtUuid);
            return 0;
        }
        return -1;
    }

    public Session getSessionByAgtUuid(UUID agtUuid) {
        return agtUuidToSession.get(agtUuid);
    }

    public UUID getAgtUuidBySessionId(String sessionId) {
        return sessionIdToAgtUuid.get(sessionId);
    }
}

