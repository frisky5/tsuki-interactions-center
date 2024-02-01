package interactions_center.agents_manager.functions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import interactions_center.agents_manager.functions.agent_state_fns.AgentStatesSubCmdToFnsMap;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;

import java.util.HashMap;
import java.util.function.Consumer;

import static interactions_center.agents_manager.constants.CMDs.CMD_AGENT_STATE;

@ApplicationScoped
public class AgentManagerCmdToSubCmdMapMap {
    private final HashMap<Integer, HashMap<Integer, Consumer<WsMessage>>> map = new HashMap<>(20);

    @Inject
    private AgentStatesSubCmdToFnsMap agentStatesSubCmdToFnsMap;

    public AgentManagerCmdToSubCmdMapMap(AgentStatesSubCmdToFnsMap agentStatesSubCmdToFnsMap) {
        map.put(CMD_AGENT_STATE, agentStatesSubCmdToFnsMap.getMap());
    }

    public HashMap<Integer, Consumer<WsMessage>> get(Integer cmd) {
        return map.get(cmd);
    }

}
