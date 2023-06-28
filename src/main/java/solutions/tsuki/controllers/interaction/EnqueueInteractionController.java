package solutions.tsuki.controllers.interaction;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import solutions.tsuki.configuration.SingleThreadExecutor;
import solutions.tsuki.functions.interaction.EnqueueInteractionFunction;
import solutions.tsuki.json.requests.InteractionRequest;
import solutions.tsuki.json.responses.InteractionResponse;

@ApplicationScoped
@Path("/v1/interaction/enqueue")
public class EnqueueInteractionController {

  @Inject
  public EnqueueInteractionFunction enqueueInteractionFunction;
  @Inject
  public SingleThreadExecutor singleThreadExecutor;

  @POST
  public Uni<InteractionResponse> enqueue(InteractionRequest request) {
    request.setRequestedAt(LocalDateTime.now(ZoneId.of("UTC")));
    return Uni.createFrom().item(request).onItem().transform(enqueueInteractionFunction)
        .runSubscriptionOn(singleThreadExecutor.getExecutor());
  }

}
