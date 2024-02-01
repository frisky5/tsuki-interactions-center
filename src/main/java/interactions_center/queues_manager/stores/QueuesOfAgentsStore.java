package interactions_center.queues_manager.stores;

import interactions_center.queues_manager.comparators.agent.TypeOne;
import interactions_center.queues_manager.queues.QueueOfAgents;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class QueuesOfAgentsStore {
    private final String SELECT_QUEUES = "SELECT * FROM queues";
    private final String ID_COLUMN = "id";
    private final String TYPE_COLUMN = "type";
    private final String QUEUEING_LOGIC_COLUMN = "agents_queuing_logic";
    private final String NAME_COLUMN = "name";

    private final Logger logger = LoggerFactory.getLogger(QueuesOfAgentsStore.class);
    private final ConcurrentHashMap<UUID, QueueOfAgents> store = new ConcurrentHashMap<>(20);


    private PgPool pgClient;

    public QueuesOfAgentsStore(PgPool pgClient) {
        this.pgClient = pgClient;
        logger.info("initializing queues of agents store");
        this.pgClient.query(SELECT_QUEUES).execute().await().indefinitely().iterator().forEachRemaining(row -> {
            QueueOfAgents queue = new QueueOfAgents(row.getUUID(ID_COLUMN));
            queue.setName(row.getString(NAME_COLUMN));
            queue.setType(row.getInteger(TYPE_COLUMN));
            switch (row.getInteger(QUEUEING_LOGIC_COLUMN)) {
                default:
                    queue.setComparator(new TypeOne(row.getUUID(ID_COLUMN)));
                    break;
            }
            put(queue);
            logger.info(
                    "queue of agents with id: [{}], name [{}], logic [{}], type [{}] was added to queues of " +
                            "interaction store.",
                    queue.getId(), row.getString(NAME_COLUMN), row.getInteger(QUEUEING_LOGIC_COLUMN),
                    queue.getType());
        });
    }

    public void put(QueueOfAgents queue) {
        store.put(queue.getId(), queue);
    }

    public QueueOfAgents get(UUID id) {
        return store.get(id);
    }

    public Set<UUID> getKeySet() {
        return store.keySet();
    }
}
