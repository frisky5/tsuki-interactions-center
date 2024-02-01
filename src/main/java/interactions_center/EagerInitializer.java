package interactions_center;

import interactions_center.agents_manager.stores.AgentsStore;
import interactions_center.queues_manager.stores.QueuesOfInteractionsStore;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.agents_manager.functions.AgentManagerCmdToSubCmdMapMap;
import interactions_center.config_manager.ConfigStore;
import interactions_center.interactions_manager.stores.InteractionsStore;
import interactions_center.queues_manager.stores.QueuesOfAgentsStore;

import static interactions_center.agents_manager.constants.CMDs.CMD_AGENT_STATE;

@ApplicationScoped
public class EagerInitializer {
    private final Logger logger = LoggerFactory.getLogger(EagerInitializer.class);

    @Inject
    AgentsStore agentsStore;

    @Inject
    InteractionsStore interactionsStore;

    @Inject
    QueuesOfAgentsStore queuesOfAgentsStore;

    @Inject
    QueuesOfInteractionsStore queuesOfInteractionsStore;

    @Inject
    AgentManagerCmdToSubCmdMapMap agentManagerCmdToSubCmdMapMap;

    @Inject
    ConfigStore configStore;
    void onStart(@Observes StartupEvent ev) {
        logger.info("forcing eager init of queues");
        agentsStore.getKeySet();
        queuesOfAgentsStore.getKeySet();
        queuesOfInteractionsStore.getKeySet();
        interactionsStore.getKeySet();
        agentManagerCmdToSubCmdMapMap.get(CMD_AGENT_STATE);
        configStore.get("keycloak_realm_name");
    }

    void onStop(@Observes ShutdownEvent ev) {
        logger.info("The application is stopping...");
    }
}
