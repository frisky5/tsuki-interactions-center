package interactions_center.agents_manager.functions.agent_state_fns;

import interactions_center.agents_manager.constants.SUB_CMDs_AGENT_STATE;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.function.Consumer;

@ApplicationScoped
public class AgentStatesSubCmdToFnsMap {
    private final DummyFn dummyFn = new DummyFn();
    private final HashMap<Integer, Consumer<WsMessage>> map = new HashMap<>(10);


    private SetLogoutFn setLogoutFn;
    private SetNotReadyFn setNotReadyFn;
    private SetReadyFn setReadyFn;
    private GetCurrentStateFn getCurrentStateFn;

    public AgentStatesSubCmdToFnsMap(SetLogoutFn setLogoutFn, SetNotReadyFn setNotReadyFn, SetReadyFn setReadyFn, GetCurrentStateFn getCurrentStateFn) {
        map.put(SUB_CMDs_AGENT_STATE.SUB_CMD_AGENT_STATE_SET_LOGOUT, setLogoutFn);
        map.put(SUB_CMDs_AGENT_STATE.SUB_CMD_AGENT_STATE_SET_NOT_READY, setNotReadyFn);
        map.put(SUB_CMDs_AGENT_STATE.SUB_CMD_AGENT_STATE_SET_READY, setReadyFn);
        map.put(SUB_CMDs_AGENT_STATE.SUB_CMD_AGENT_STATE_GET_STATE, getCurrentStateFn);
    }

    public HashMap<Integer, Consumer<WsMessage>> getMap() {
        return map;
    }
}
