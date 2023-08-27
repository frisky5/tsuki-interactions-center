package solutions.tsuki.controllers.interaction;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import solutions.tsuki.configuration.ExecutorsFactory;
import solutions.tsuki.functions.interaction.EnqueueInteractionFunction;
import solutions.tsuki.json.requests.InteractionRequest;
import solutions.tsuki.json.responses.InteractionResponse;
import solutions.tsuki.stores.InteractionsStore;
import solutions.tsuki.stores.QueuesOfInteractionsStore;

@ApplicationScoped
@Path("/v1/interaction/enqueue")
public class EnqueueInteractionController {

  @Inject
  public EnqueueInteractionFunction enqueueInteractionFunction;

  @Inject
  ExecutorsFactory executorsFactory;

  @Inject
  InteractionsStore interactionsStore;
  @Inject
  QueuesOfInteractionsStore queuesOfInteractionsStore;

  @POST
  public Uni<InteractionResponse> enqueue(InteractionRequest request) {
    request.setRequestedAt(LocalDateTime.now(ZoneId.of("UTC")));
    return Uni.createFrom().item(request).onItem().transform((request1 -> {
          if (!interactionsStore.contains(request1.getId())) {
            InteractionResponse response = new InteractionResponse();
            response.setError(true);
            response.setMessage("interaction not found in store");
            return response;
          }
          if (!queuesOfInteractionsStore.contains(request1.getQueueId())) {
            InteractionResponse response = new InteractionResponse();
            response.setError(true);
            response.setMessage("queue not found in queues of interactions store");
            return response;
          }
          return enqueueInteractionFunction.apply(request1);

        }))
        .runSubscriptionOn(executorsFactory.getQqiSingleThreadExecutor());
  }

}
