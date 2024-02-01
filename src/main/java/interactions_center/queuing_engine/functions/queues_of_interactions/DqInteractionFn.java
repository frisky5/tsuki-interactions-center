package interactions_center.queuing_engine.functions.queues_of_interactions;

import interactions_center.queues_manager.queues.QueueOfInteractions;
import interactions_center.queues_manager.stores.QueuesOfInteractionsStore;
import interactions_center.queuing_engine.executors.QueuesSingleThreadExecutor;
import interactions_center.queuing_engine.utils.QueuingEngineOutput;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.interactions_manager.constants.INTERACTION_STATE;
import interactions_center.interactions_manager.stores.InteractionsStore;
import interactions_center.interactions_manager.stores.items.Interaction;
import interactions_center.queuing_engine.json.requests.DequeueInteractionRequest;
import interactions_center.queuing_engine.runnables.ProcessQueuesRunnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

@ApplicationScoped
public class DqInteractionFn implements
        Function<DequeueInteractionRequest, QueuingEngineOutput> {

    private final Logger logger = LoggerFactory.getLogger(DqInteractionFn.class);
    private final String INSERT_DEQUEUE_EVENT_QUERY =
            "INSERT INTO interactions_events(interaction_id, state, " +
                    "event_timestamp) VALUES ($1,$2,$3) RETURNING id";

    private QueuesSingleThreadExecutor qste;
    private QueuesOfInteractionsStore qis;
    private ProcessQueuesRunnable pqr;
    private InteractionsStore interactionsStore;
    private PgPool pgPool;

    public DqInteractionFn(QueuesSingleThreadExecutor qste, QueuesOfInteractionsStore qis, ProcessQueuesRunnable pqr, InteractionsStore interactionsStore, PgPool pgPool) {
        this.qste = qste;
        this.qis = qis;
        this.pqr = pqr;
        this.interactionsStore = interactionsStore;
        this.pgPool = pgPool;
    }

    @Override
    public QueuingEngineOutput apply(DequeueInteractionRequest request) {
        QueuingEngineOutput output = new QueuingEngineOutput();
        Interaction interaction = interactionsStore.get(request.getInteractionId());

        if (Objects.isNull(interaction)) {
            output.setError(true);
            output.setMessage("interaction not found");
            return output;
        }

        interaction.lock();
        logger.info("LOCKED interaction [{}]", interaction.getId());
        try {
            QueueOfInteractions queueOfInteractions = qis.get(request.getQueueId());

            if (Objects.isNull(queueOfInteractions)) {
                output.setError(true);
                output.setMessage("requested interaction queue id not found");
                return output;
            }

            queueOfInteractions.lock();
            logger.info("LOCKED queue of interactions [{}]", queueOfInteractions.getId());

            try {
                if (Arrays.stream(INTERACTION_STATE.ALLOWED_TO_DEQUEUE)
                        .noneMatch(state -> state == interaction.getState())) {
                    output.setError(true);
                    output.setMessage("interaction is in state that prevents it from dequeue");
                    return output;
                }

                Number generatedEventId = pgPool.preparedQuery(INSERT_DEQUEUE_EVENT_QUERY)
                        .execute(Tuple.of(request.getInteractionId(), INTERACTION_STATE.DEQUEUED,
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
                    output.setMessage("failed to insert dequeue event into DB");
                    return output;
                }

                interaction.setState(INTERACTION_STATE.DEQUEUED);

                queueOfInteractions.remove(interaction);

                output.setEventId(generatedEventId.longValue());
                output.setError(false);

                qste.getExecutor().execute(pqr);
                return output;
            } finally {
                queueOfInteractions.unlock();
                logger.info("UNLOCKED queue of interactions [{}]", queueOfInteractions.getId());
            }
        } finally {
            interaction.unlock();
            logger.info("UNLOCKED interaction [{}]", interaction.getId());
        }

    }
}
