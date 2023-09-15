package solutions.tsuki.configuration;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.agents.constants.AGENT_STATES;
import solutions.tsuki.ic.interactions.constants.INTERACTION_STATE;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;
import solutions.tsuki.ic.queues.manager.queues.item.Agent;
import solutions.tsuki.ic.queues.manager.queues.item.Interaction;
import solutions.tsuki.ic.queues.manager.queues.item.comparators.agent.TypeOne;
import solutions.tsuki.ic.agents.manager.stores.AgentsStore;
import solutions.tsuki.ic.interactions.manager.stores.InteractionsStore;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesOfAgentsStore;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesOfInteractionsStore;

@ApplicationScoped
public class OnStartupInit {

  public final Logger logger = LoggerFactory.getLogger("On Startup Initialization");
  @Inject
  public PgPool client;
  @Inject
  public AgentsStore agentsStore;
  @Inject
  public QueuesOfAgentsStore queuesOfAgentsStore;
  @Inject
  public QueuesOfInteractionsStore queuesOfInteractionsStore;
  @Inject
  public InteractionsStore interactionsStore;

  public void Initialize(@Observes StartupEvent event) {
    logger.info("initializing queues of agents");
    client.query("SELECT * FROM queues_of_interactions").execute().await().indefinitely().iterator()
        .forEachRemaining(row -> {
          QueueOfAgents queueOfAgents = new QueueOfAgents(row.getInteger("id"),
              new TypeOne(row.getInteger("id")));
          queuesOfAgentsStore.putQueue(queueOfAgents);
          logger.info("queue of agents id: [{}], name [{}] was added to queues of agents store.",
              queueOfAgents.getId(), row.getString("name"));
        });

    logger.info("initializing agents store");
    client.query("SELECT * FROM agents").execute().await().indefinitely().iterator()
        .forEachRemaining(row -> {
          Agent agent = new Agent(row.getUUID("keycloak_uuid"));
          agent.setState(AGENT_STATES.LOGGED_OUT);
          client.preparedQuery(
                  "SELECT agent_keycloak_id, queue_of_interactions_id, priority FROM agents_to_queues_of_interactions_map WHERE agent_keycloak_id=$1")
              .execute(Tuple.of(agent.getKeycloakUserUuid())).await().indefinitely().iterator()
              .forEachRemaining(mapRow -> {
                logger.info(
                    "assigning queue [{}] to agent [{}] with priority [{}]",
                    mapRow.getInteger("queue_of_interactions_id"), mapRow.getUUID("keycloak_uuid"),
                    mapRow.getInteger("priority"));
                agent.assignQueue(queuesOfInteractionsStore.get(mapRow.getInteger("queue_of_interactions_id")),
                    mapRow.getInteger("priority"));
              });
          agentsStore.put(agent);
          logger.info("agent [{}] added to agents store", agent.getKeycloakUserUuid());
        });

    logger.info("initializing interactions store");
    client.preparedQuery(
            "SELECT id, type, current_state, created_at FROM interactions WHERE current_state=$1")
        .execute(Tuple.of(
            INTERACTION_STATE.NEW)).await().indefinitely().iterator().forEachRemaining(row -> {
          Interaction interaction = new Interaction(row.getLong("id"));
          interaction.setState(row.getInteger("current_state"));
          interaction.setType(row.getInteger("type"));
          interaction.getTimeMeasurements().setCreatedAt(row.getLocalDateTime("created_at"));
          logger.info("adding interaction [{}], created at [{}] of type [{}]",
              interaction.getId(),
              interaction.getTimeMeasurements().getCreatedAt(),
              interaction.getType());
          interactionsStore.put(interaction);
        });
  }
}
