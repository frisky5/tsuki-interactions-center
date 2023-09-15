package solutions.tsuki.ic.queues.manager.webservices;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import solutions.tsuki.functions.interaction.EnqueueInteractionFunction;
import solutions.tsuki.ic.interactions.manager.stores.InteractionsStore;
import solutions.tsuki.ic.queues.manager.queues.stores.QueuesStores;
import solutions.tsuki.ic.queues.manager.utils.ExecutorsFactory;
import solutions.tsuki.json.requests.InteractionRequest;
import solutions.tsuki.json.responses.InteractionResponse;

@ApplicationScoped
@Path("/v1/interaction/enqueue")
public class EnqueueInteractionController {

  @Inject
  public EnqueueInteractionFunction enqueueInteractionFunction;

  @Inject
  ExecutorsFactory executorsFactory;

  @Inject
  QueuesStores queuesStores;

  @Inject
  InteractionsStore interactionsStore;

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
          if (!queuesStores.getQueuesOfInteractionsStore().contains(request1.getQueueId())) {
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
