package interactions_center.agents_manager.stores;

import interactions_center.queues_manager.stores.QueuesOfInteractionsStore;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.agents_manager.constants.AGENT_STATES;
import interactions_center.agents_manager.stores.items.Agent;
import interactions_center.config_manager.ConfigStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static interactions_center.config_manager.CONFIG_STORE_KEYS.*;

@ApplicationScoped
public class AgentsStore {
    private final Logger LOG = LoggerFactory.getLogger(AgentsStore.class);

    private final String SELECT_AGENT_ASSIGNED_QUEUES = "SELECT * FROM map_agents_to_queues WHERE agent_id=$1";
    private final String QUEUE_ID_COLUMN = "queue_id";
    private final String PRIORITY_COLUMN = "priority";

    private final ConcurrentHashMap<UUID, Agent> store = new ConcurrentHashMap<>();

    private final PgPool pgClient;
    private final QueuesOfInteractionsStore queuesOfInteractionsStore;

    private ConfigStore configStore;

    public AgentsStore(Keycloak keycloak, PgPool pgClient, QueuesOfInteractionsStore queuesOfInteractionsStore, ConfigStore configStore) {
        this.pgClient = pgClient;
        this.queuesOfInteractionsStore = queuesOfInteractionsStore;

        LOG.info("initializing agents store");

        String realmName = configStore.get(KEYCLOAK_REALM_NAME);
        String agentsGroupName = configStore.get(KEYCLOAK_AGENTS_GROUP_NAME);

        RealmResource icRealm;

        try {
            icRealm = keycloak.realm(realmName);
        } catch (Exception e) {
            LOG.error("failed to get realm with name [{}], agents store will not initialize", realmName, e);
            return;
        }

        GroupResource agentsGroup;
        String agentsGroupId;

        try {
            GroupRepresentation agentsGroupRep =
                    icRealm.groups().groups().stream().filter(groupRepresentation -> groupRepresentation.getName().equals(agentsGroupName)).findFirst().orElse(null);
            if (Objects.isNull(agentsGroupRep)) {
                LOG.error("failed to get agents group with name [{}], agents store will not initialize", agentsGroupName);
                return;
            }
            agentsGroupId = agentsGroupRep.getId();
        } catch (Exception e) {
            LOG.error("failed to get agents group with name [{}], agents store will not initialize", agentsGroupName, e);
            return;
        }

        try {
            agentsGroup = icRealm.groups().group(agentsGroupId);
            if (Objects.isNull(agentsGroup)) {
                LOG.error("failed to get agents group by id [{}], agents store will not initialize",
                        agentsGroupId);
                return;
            }
        } catch (Exception e) {
            LOG.error("failed to get agents group by id [{}], agents store will not initialize",
                    agentsGroupId, e);
            return;
        }

        List<UserRepresentation> agents;
        try {
            agents = agentsGroup.members();
            if (Objects.isNull(agents)) {
                LOG.error("failed to get members (agents) from the agents group either no users are assigned to " +
                        "agents group or keycloak failed to retrieve, agents store" +
                        " will not initialize");
                return;
            }
            if (agents.isEmpty()) {
                LOG.warn("agents group contains no users, make sure you add users to it for the agents store to " +
                        "initialize");
                return;
            }
        } catch (Exception e) {
            LOG.error("failed to get members (agents) from the agents group, agents store will not initialize", e);
            return;
        }

        agents.forEach(agent -> {
            Agent agentToPut = new Agent(UUID.fromString(agent.getId()));
            agentToPut.setState(AGENT_STATES.LOGOUT);
            put(agentToPut);
            LOG.info("agent [{}] added to store with initial state of LOGOUT", agentToPut.getId());

            this.pgClient.preparedQuery(SELECT_AGENT_ASSIGNED_QUEUES)
                    .execute(Tuple.of(agent.getId())).await().indefinitely().iterator().forEachRemaining(innerRow -> {
                        agentToPut.assignQueue(
                                this.queuesOfInteractionsStore.get(innerRow.getUUID(QUEUE_ID_COLUMN)),
                                innerRow.getInteger(PRIORITY_COLUMN));
                        LOG.info(
                                "assigned queue [{}] to agent [{}] with priority [{}]",
                                innerRow.getUUID(QUEUE_ID_COLUMN), agentToPut.getId(),
                                innerRow.getInteger(PRIORITY_COLUMN));
                    });
        });
    }

    public void put(Agent agent) {
        store.put(agent.getId(), agent);
    }

    public Agent get(UUID id) {
        return store.get(id);
    }

    public Agent get(String id) {
        try {
            return store.get(UUID.fromString(id));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean contains(UUID id) {
        return store.containsKey(id);
    }

    public boolean contains(String id) {
        try {
            return store.containsKey(UUID.fromString(id));
        } catch (Exception e) {
            return false;
        }
    }

    public Set<UUID> getKeySet() {
        return store.keySet();
    }

    public Enumeration<UUID> getKeys() {
        return store.keys();
    }
}
