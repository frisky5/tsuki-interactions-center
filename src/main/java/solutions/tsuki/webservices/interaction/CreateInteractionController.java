package solutions.tsuki.webservices.interaction;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import solutions.tsuki.ic.queues.manager.utils.ExecutorsFactory;
import solutions.tsuki.functions.interaction.CreateInteractionFunction;
import solutions.tsuki.json.requests.InteractionRequest;
import solutions.tsuki.json.responses.InteractionResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
@Path("/v1/interaction/create")
public class CreateInteractionController {

    @Inject
    CreateInteractionFunction createInteractionFunction;
    @Inject
    ExecutorsFactory executorsFactory;

    @POST
    public Uni<InteractionResponse> createInteraction(InteractionRequest request) {
        request.setRequestedAt(LocalDateTime.now(ZoneId.of("UTC")));
        return Uni.createFrom().item(request).onItem().transform(createInteractionFunction)
                .runSubscriptionOn(executorsFactory.getQqiSingleThreadExecutor());
    }
}
