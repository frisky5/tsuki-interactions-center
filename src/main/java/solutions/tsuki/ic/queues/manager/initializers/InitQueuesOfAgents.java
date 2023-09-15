package solutions.tsuki.ic.queues.manager.initializers;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.queues.agents.QueueOfAgents;
import solutions.tsuki.ic.queues.manager.queues.item.comparators.agent.TypeOne;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;

@ApplicationScoped
public class InitQueuesOfAgents {

  private final String SELECT_QUEUES = "SELECT * FROM queues";
  private final String QUEUE_ID_COLUMN_NAME = "id";
  private final String TYPE_COLUMN_NAME = "type";
  private final String QUEUEING_LOGIC_COLUMN_NAME = "agents_queuing_logic";
  private final String QUEUE_NAME_COLUMN_NAME = "name";

  public final Logger logger = LoggerFactory.getLogger(InitQueuesOfAgents.class);

  @Inject
  public PgPool pgClient;

  @Inject
  public QueuesStores queuesStores;

  @Inject
  public TypeOne typeOne;

  public void Initialize(@Observes StartupEvent event) {
    logger.info("initializing queues of agents");

    pgClient.query(SELECT_QUEUES).execute().await().indefinitely().iterator()
        .forEachRemaining(row -> {
          QueueOfAgents queue = new QueueOfAgents(row.getInteger(QUEUE_ID_COLUMN_NAME));
          queue.setName(row.getString(QUEUE_NAME_COLUMN_NAME));
          queue.setType(row.getInteger(TYPE_COLUMN_NAME));
          switch (row.getInteger(QUEUEING_LOGIC_COLUMN_NAME)) {
            default:
              queue.setComparator(typeOne);
              break;
          }
          queuesStores.getQueuesOfAgentsStore().putQueue(queue);
          logger.info(
              "queue of interactions with id: [{}], name [{}], logic [{}], type [{}] was added to queues of interaction store.",
              queue.getId(), row.getString("name"), row.getInteger("queuing_logic"),
              queue.getType());
        });
  }
}
