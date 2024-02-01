package interactions_center.queues_manager.stores;

import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.queues_manager.comparators.interaction.TypeOne;
import interactions_center.queues_manager.comparators.interaction.TypeTwo;
import interactions_center.queues_manager.queues.QueueOfInteractions;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class QueuesOfInteractionsStore {
    private final Logger logger = LoggerFactory.getLogger(QueuesOfInteractionsStore.class);

    private final String SELECT_QUEUES = "SELECT * FROM queues";
    private final String ID_COLUMN = "id";
    private final String QUEUEING_LOGIC_COLUMN = "interactions_queuing_logic";
    private final String TYPE_COLUMN = "type";
    private final String NAME_COLUMN = "name";

    private final ConcurrentHashMap<UUID, QueueOfInteractions> store = new ConcurrentHashMap<>();

    private PgPool pgClient;

    public QueuesOfInteractionsStore(PgPool pgClient) {
        this.pgClient = pgClient;
        logger.info("initializing queues of interactions store");
        this.pgClient.query(SELECT_QUEUES).execute().await().indefinitely().iterator().forEachRemaining(row -> {
            QueueOfInteractions queueOfInteractions = new QueueOfInteractions(
                    row.getUUID(ID_COLUMN), row.getInteger(TYPE_COLUMN));

            queueOfInteractions.setName(row.getString(NAME_COLUMN));

            switch (row.getInteger(QUEUEING_LOGIC_COLUMN)) {
                case 2:
                    queueOfInteractions.setComparator(new TypeTwo(row.getUUID(ID_COLUMN)));
                    break;
                default:
                    queueOfInteractions.setComparator(new TypeOne(row.getUUID(ID_COLUMN)));
                    break;
            }

            put(queueOfInteractions);

            logger.info(
                    "queue of interactions with id: [{}], name [{}], logic [{}], type [{}] was added to queues of interaction store.",
                    queueOfInteractions.getId(), row.getString(NAME_COLUMN), row.getInteger(QUEUEING_LOGIC_COLUMN),
                    queueOfInteractions.getType());
        });
    }


    public void put(QueueOfInteractions queue) {
        store.put(queue.getId(), queue);
    }

    public QueueOfInteractions get(UUID id) {
        return store.get(id);
    }

    public boolean contains(UUID id) {
        return store.containsKey(id);
    }

    public Set<UUID> getKeySet() {
        return store.keySet();
    }
}
