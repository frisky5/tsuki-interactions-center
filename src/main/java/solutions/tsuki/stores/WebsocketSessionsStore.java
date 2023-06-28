package solutions.tsuki.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WebsocketSessionsStore {

  public Map<String, Session> sessions = new ConcurrentHashMap<>();

  public void put(String userId, Session session) {
    sessions.put(userId, session);
  }

  public void remove(String userId) {
    sessions.remove(userId);
  }

  public boolean containsUserId(String userId) {
    return sessions.containsKey(userId);
  }
}
