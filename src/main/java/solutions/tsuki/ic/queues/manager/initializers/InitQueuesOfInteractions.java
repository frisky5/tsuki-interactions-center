package solutions.tsuki.ic.queues.manager.initializers;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.ic.queues.manager.queues.item.comparators.interaction.TypeOne;
import solutions.tsuki.ic.queues.manager.queues.item.comparators.interaction.TypeTwo;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;
import solutions.tsuki.ic.queues.manager.queues.interactions.QueueOfInteractions;

@ApplicationScoped
public class InitQueuesOfInteractions {

  private final String SELECT_QUEUES = "SELECT * FROM queues";
  private final String QUEUEING_LOGIC_COLUMN_NAME = "interactions_queuing_logic";
  private final String TYPE_COLUMN_NAME = "type";
  private final String QUEUE_ID_COLUMN_NAME = "id";
  private final String QUEUE_NAME_COLUMN_NAME = "name";

  public final Logger logger = LoggerFactory.getLogger(InitQueuesOfInteractions.class);

  @Inject
  public PgPool pgClient;

  @Inject
  public QueuesStores queuesStores;

  @Inject
  public TypeOne typeOne;

  @Inject
  public TypeTwo typeTwo;

  public void Initialize(@Observes StartupEvent event) {
    logger.info("initializing queues of interactions");

    pgClient.query(SELECT_QUEUES).execute().await().indefinitely().iterator()
        .forEachRemaining(row -> {
          QueueOfInteractions queueOfInteractions = new QueueOfInteractions(
              row.getInteger(QUEUE_ID_COLUMN_NAME), row.getInteger(TYPE_COLUMN_NAME));

          queueOfInteractions.setName(row.getString(QUEUE_NAME_COLUMN_NAME));

          switch (row.getInteger(QUEUEING_LOGIC_COLUMN_NAME)) {
            case 2:
              queueOfInteractions.setComparator(typeTwo);
              break;
            default:
              queueOfInteractions.setComparator(typeOne);
              break;
          }
          queuesStores.getQueuesOfInteractionsStore().putQueue(queueOfInteractions);
          logger.info(
              "queue of interactions with id: [{}], name [{}], logic [{}], type [{}] was added to queues of interaction store.",
              queueOfInteractions.getId(), row.getString("name"), row.getInteger("queuing_logic"),
              queueOfInteractions.getType());
        });
  }

}
