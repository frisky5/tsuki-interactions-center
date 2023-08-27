package solutions.tsuki.functions.interaction;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.constants.INTERACTION_STATE;
import solutions.tsuki.functions.queue.queueOfInteractions.ProcessFunction;
import solutions.tsuki.functions.queue.queueOfInteractions.ValidateByQueueFunction;
import solutions.tsuki.json.requests.InteractionRequest;
import solutions.tsuki.json.responses.InteractionResponse;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.queue.interactions.QueueOfInteractions;
import solutions.tsuki.stores.StoresDTO;

@ApplicationScoped
public class EnqueueInteractionFunction implements
    Function<InteractionRequest, InteractionResponse> {

  public final Logger logger = LoggerFactory.getLogger("EnqueueInteraction Function");
  public final String INSERT_ENQUEUE_EVENT_SQL =
      "INSERT INTO interactions_events(interaction_id, state, queue_id, " +
          "event_timestamp) VALUES ($1,$2,$3,$4) RETURNING id";
  @Inject
  ExecutorsFactory executorsFactory;

  @Inject
  StoresDTO storesDTO;

  @Inject
  PgPool pgPool;

  @Inject
  ValidateByQueueFunction validateByQueueFunction;

  @Inject
  ProcessFunction processFunction;

  @Override
  public InteractionResponse apply(InteractionRequest request) {
    InteractionResponse response = new InteractionResponse();
    Interaction interaction = storesDTO.getInteractionsStore().get(request.getId());

    if (Objects.isNull(interaction)) {
      response.setError(true);
      response.setMessage("interaction not found");
      return response;
    }

    if (Objects.isNull(request.getQueueId())) {
      response.setError(true);
      response.setMessage("provide a queue id");
      return response;
    }

    QueueOfInteractions queueOfInteractions = storesDTO.getQueuesOfInteractionsStore()
        .get(request.getQueueId());

    if (Objects.isNull(queueOfInteractions)) {
      response.setError(true);
      response.setMessage("requested interaction queue id not found");
      return response;
    }

    boolean allowed = Arrays.stream(INTERACTION_STATE.ALLOWED_TO_ENQUEUE)
        .anyMatch(state -> state == interaction.getState());
    if (!allowed) {
      response.setError(true);
      response.setMessage("interaction is not allowed to be enqueued");
      return response;
    }

    Number generatedEventId = pgPool.preparedQuery(INSERT_ENQUEUE_EVENT_SQL)
        .execute(Tuple.of(request.getId(), INTERACTION_STATE.QUEUED, request.getQueueId(),
            request.getRequestedAt()))
        .onItemOrFailure().transform((rowSet, throwable) -> {
          if (throwable != null) {
            logger.error("interaction [{}] failed to write enqueue event to DB", request.getId(),
                throwable);
            return -1;
          }
          return rowSet.iterator().next().getLong("id");
        }).await().indefinitely();

    if (generatedEventId.longValue() == -1) {
      response.setError(true);
      response.setMessage("failed to create enqueue event into DB");
      return response;
    }

    interaction.setQueueId(request.getQueueId());
    interaction.setPriority(request.getPriority());
    queueOfInteractions.sortedInsert(interaction);
    interaction.setState(INTERACTION_STATE.QUEUED);

    executorsFactory.getQqiSingleThreadExecutor().execute(() -> {
      validateByQueueFunction.apply(queueOfInteractions.getId());
    });
    executorsFactory.getQqiSingleThreadExecutor().execute(() -> {
      processFunction.apply(null);
    });
    response.setError(false);
    response.setEventId(generatedEventId.longValue());
    return response;
  }
}
