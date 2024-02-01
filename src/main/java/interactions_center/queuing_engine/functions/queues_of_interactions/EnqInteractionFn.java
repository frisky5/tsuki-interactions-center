package interactions_center.queuing_engine.functions.queues_of_interactions;

import interactions_center.interactions_manager.constants.INTERACTION_STATE;
import interactions_center.interactions_manager.stores.items.Interaction;
import interactions_center.queues_manager.queues.QueueOfInteractions;
import interactions_center.queues_manager.stores.QueuesOfInteractionsStore;
import interactions_center.queuing_engine.executors.QueuesSingleThreadExecutor;
import interactions_center.queuing_engine.json.requests.EnqueueInteractionRequest;
import interactions_center.queuing_engine.utils.QueuingEngineOutput;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.interactions_manager.stores.InteractionsStore;
import interactions_center.queuing_engine.runnables.ProcessQueuesRunnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

@ApplicationScoped
public class EnqInteractionFn implements
        Function<EnqueueInteractionRequest, QueuingEngineOutput> {

    private final Logger logger = LoggerFactory.getLogger(EnqInteractionFn.class);
    private final String INSERT_ENQUEUE_EVENT_QUERY =
            "INSERT INTO interactions_events(interaction_id, state, queue_id, " +
                    "event_timestamp) VALUES ($1,$2,$3,$4) RETURNING id";

    private QueuesSingleThreadExecutor qste;
    private ProcessQueuesRunnable pqr;
    private QueuesOfInteractionsStore qis;
    private InteractionsStore interactionsStore;
    private PgPool pgPool;


    public EnqInteractionFn(QueuesSingleThreadExecutor qste, ProcessQueuesRunnable pqr,
                            QueuesOfInteractionsStore qis, InteractionsStore interactionsStore, PgPool pgPool) {
        this.qste = qste;
        this.pqr = pqr;
        this.qis = qis;
        this.interactionsStore = interactionsStore;
        this.pgPool = pgPool;
    }

    @Override
    public QueuingEngineOutput apply(EnqueueInteractionRequest request) {
        QueuingEngineOutput output = new QueuingEngineOutput();
        Interaction interaction = interactionsStore.get(request.getInteractionId());

        if (Objects.isNull(interaction)) {
            output.setError(true);
            output.setMessage("interaction not found");
            return output;
        }

        if (Objects.isNull(request.getQueueId())) {
            output.setError(true);
            output.setMessage("provide a queue id");
            return output;
        }

        QueueOfInteractions queueOfInteractions = qis.get(request.getQueueId());


        if (Objects.isNull(queueOfInteractions)) {
            output.setError(true);
            output.setMessage("requested interaction queue id not found");
            return output;
        }

        try {
            queueOfInteractions.lock();
            logger.info("LOCKED queue of interactions [{}]", queueOfInteractions.getId());

            try {
                interaction.lock();
                logger.info("LOCKED interaction [{}]", interaction.getId());

                if (Arrays.stream(INTERACTION_STATE.ALLOWED_TO_ENQUEUE)
                        .noneMatch(state -> state == interaction.getState())) {
                    output.setError(true);
                    output.setMessage("interaction is in state that prevents it from enqueuing");
                    return output;
                }

                Number generatedEventId = pgPool.preparedQuery(INSERT_ENQUEUE_EVENT_QUERY)
                        .execute(Tuple.of(request.getInteractionId(), INTERACTION_STATE.QUEUED, request.getQueueId(),
                                request.getRequestedAt()))
                        .onItemOrFailure().transform((rowSet, throwable) -> {
                            if (throwable != null) {
                                logger.error("failed to insert interaction [{}] enqueue event to DB.",
                                        request.getInteractionId(),
                                        throwable);
                                return -1;
                            }
                            return rowSet.iterator().next().getLong("id");
                        }).await().indefinitely();

                if (generatedEventId.longValue() < 0) {
                    output.setError(true);
                    output.setMessage("failed to insert enqueue event into DB");
                    return output;
                }

                interaction.setQueueId(request.getQueueId());
                interaction.setState(INTERACTION_STATE.QUEUED);
                interaction.setPriorityOnQueue(request.getPriority());

                queueOfInteractions.sortedInsert(interaction);

                output.setEventId(generatedEventId.longValue());
                output.setError(false);

                qste.getExecutor().execute(pqr);

                return output;
            } finally {
                interaction.unlock();
                logger.info("UNLOCKED interaction [{}]", interaction.getId());
            }
        } finally {
            queueOfInteractions.unlock();
            logger.info("UNLOCKED queue of interactions [{}]", queueOfInteractions.getId());
        }
    }
}
