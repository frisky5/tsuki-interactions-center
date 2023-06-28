package solutions.tsuki.websockets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.stores.WebsocketSessionsStore;

@ServerEndpoint("/v1/agent/ws/{userid}")
@ApplicationScoped
public class AgentWebsocket {

  @Inject
  WebsocketSessionsStore websocketSessionsStore;
  public Logger logger = LoggerFactory.getLogger("AgentWebsocket");

  @OnOpen
  public void onOpen(Session session, @PathParam("userid") String userid) {
    if (websocketSessionsStore.containsUserId(userid)) {
      try {
        session.close();
      } catch (IOException e) {
        logger.error(
            "failed to close websocket session [{}] when trying to close it because there is already another websocket for the same user opened",
            session.getId());
      }
      return;
    }
    websocketSessionsStore.put(userid, session);
  }

  @OnClose
  public void onClose(Session session, @PathParam("userid") String userid) {
    websocketSessionsStore.remove(userid);
  }

  @OnMessage
  public void onMessage(String message, @PathParam("userid") String userid) {

  }

  @OnError
  public void onError(Session session, @PathParam("userid") String userid, Throwable throwable) {
    logger.error("error on websocket [{}] of user [{}]", session.getId(), userid);
    websocketSessionsStore.remove(userid);
  }
}
