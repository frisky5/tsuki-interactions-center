package solutions.tsuki.configuration;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.constants.AGENT_STATES;
import solutions.tsuki.constants.INTERACTION_STATE;
import solutions.tsuki.queueItems.Agent;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.queueItems.comparators.agent.TypeOne;
import solutions.tsuki.queue.agents.QueueOfAgents;
import solutions.tsuki.queue.interactions.QueueOfInteractions;
import solutions.tsuki.stores.AgentsStore;
import solutions.tsuki.stores.InteractionsStore;
import solutions.tsuki.stores.QueuesOfAgentsStore;
import solutions.tsuki.stores.QueuesOfInteractionsStore;

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
        logger.info("                                                           \n"
                + " _____  _____  _   _  _   __ _____            _____  _____ \n"
                + "|_   _|/  ___|| | | || | / /|_   _|          |_   _|/  __ \\\n"
                + "  | |  \\ `--. | | | || |/ /   | |    ______    | |  | /  \\/\n"
                + "  | |   `--. \\| | | ||    \\   | |   |______|   | |  | |    \n"
                + "  | |  /\\__/ /| |_| || |\\  \\ _| |_            _| |_ | \\__/\\\n"
                + "  \\_/  \\____/  \\___/ \\_| \\_/ \\___/            \\___/  \\____/\n"
                + "                                                           \n");
        logger.info("initializing queues of interactions and agents");
        client.query("SELECT * FROM queues").execute().await().indefinitely().iterator()
                .forEachRemaining(row -> {
                    QueueOfInteractions queueOfInteractions = new QueueOfInteractions(row.getInteger("id"),
                            new solutions.tsuki.queueItems.comparators.interaction.TypeOne());
                    queuesOfInteractionsStore.put(queueOfInteractions);
                    logger.info("queue of interactions [{}] was added to queues of interaction store.",
                            queueOfInteractions.getId());

                    QueueOfAgents queueOfAgents = new QueueOfAgents(row.getInteger("id"),
                            new TypeOne(row.getInteger("id")));
                    queuesOfAgentsStore.put(queueOfAgents);
                    logger.info("queue of agents [{}] was added to queues of agents store.",
                            queueOfAgents.getId());
                });

        logger.info("initializing agents store");
        client.query("SELECT * FROM AGENTS").execute().await().indefinitely().iterator()
                .forEachRemaining(row -> {
                    Agent agent = new Agent(row.getUUID("id").toString());
                    agent.setState(AGENT_STATES.LOGGED_OUT);
                    client.preparedQuery(
                                    "SELECT agent_id, queue_id, priority FROM agents_queues_map WHERE agent_id=$1")
                            .execute(Tuple.of(agent.getId())).await().indefinitely().iterator()
                            .forEachRemaining(mapRow -> {
                                logger.info(
                                        "assigning queue [{}] to agent [{}] with priority [{}]",
                                        mapRow.getInteger("queue_id"), mapRow.getUUID("agent_id"),
                                        mapRow.getInteger("priority"));
                                agent.assignQueue(queuesOfInteractionsStore.get(mapRow.getInteger("queue_id")),
                                        mapRow.getInteger("priority"));
                            });
                    agentsStore.put(agent);
                    logger.info("agent [{}] added to agents store", agent.getId());
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
