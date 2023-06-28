package solutions.tsuki.functions.interaction;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.tsuki.constants.INTERACTION_STATE;
import solutions.tsuki.json.requests.InteractionRequest;
import solutions.tsuki.json.responses.InteractionResponse;
import solutions.tsuki.queueItems.Interaction;
import solutions.tsuki.stores.InteractionsStore;

@ApplicationScoped
public class CreateInteractionFunction implements
    Function<InteractionRequest, InteractionResponse> {

  public final Logger logger = LoggerFactory.getLogger("CreateInteraction Function");
  @Inject
  PgPool pgPool;
  @Inject
  InteractionsStore interactionsStore;

  @Override
  public InteractionResponse apply(InteractionRequest request) {
    Number generatedId = pgPool.preparedQuery(
            "INSERT INTO interactions(\"type\", current_state, created_at) VALUES ($1, $2, $3) RETURNING id")
        .execute(Tuple.of(request.getType(), INTERACTION_STATE.NEW, request.getRequestedAt()))
        .onItemOrFailure()
        .transform((rowSet, throwable) ->
            {
              if (throwable != null) {
                logger.error("failed to insert new interaction to DB", throwable);
                return -1;
              } else {
                return rowSet.iterator().next().getLong("id");
              }
            }
        ).await().indefinitely();

    InteractionResponse response = new InteractionResponse();
    if (generatedId.longValue() == -1) {
      response.setMessage("failed to insert interaction into DB, try again");
      response.setError(true);
      return response;
    }

    Interaction interaction = new Interaction(generatedId.longValue());
    interaction.setState(INTERACTION_STATE.NEW);
    interaction.setType(request.getType());
    interaction.getTimeMeasurements().
        setCreatedAt(request.getRequestedAt());
    interactionsStore.put(interaction);
    logger.info("created new interaction [{}], at [{}] of type[{}]", response.getId(),
        request.getRequestedAt(), request.getType());

    response.setError(false);
    response.setId(generatedId.longValue());
    return response;
  }
}

