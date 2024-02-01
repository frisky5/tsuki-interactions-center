package interactions_center.agents_manager.functions.agent_state_fns;

import interactions_center.agents_manager.json.requests.websocket.WsMessage;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Consumer;

@ApplicationScoped
public class DummyFn implements Consumer<WsMessage> {

    @Override
    public void accept(WsMessage wsMessage) {

    }
}
