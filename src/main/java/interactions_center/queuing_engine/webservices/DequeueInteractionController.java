package interactions_center.queuing_engine.webservices;

import interactions_center.queuing_engine.json.responses.QueuesGenericResponse;
import interactions_center.queuing_engine.utils.QueuingEngineOutput;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.NoCache;
import interactions_center.queuing_engine.functions.queues_of_interactions.DqInteractionFn;
import interactions_center.queuing_engine.json.requests.DequeueInteractionRequest;

@ApplicationScoped
@Path("/v1/queuing_engine/dequeue_interaction")
@NoCache
public class DequeueInteractionController {

    @Inject
    public DqInteractionFn dqInteractionFn;

    @POST
    @Blocking
    public Uni<QueuesGenericResponse> dequeue(DequeueInteractionRequest requestBody) {
        return Uni.createFrom().item(requestBody).onItem().transform((request -> {
            QueuesGenericResponse response = new QueuesGenericResponse();
            QueuingEngineOutput output = dqInteractionFn.apply(requestBody);
            response.setError(output.getCode());
            response.setCode(output.getCode());
            response.setEventId(output.getEventId());
            return response;
        }));
    }

}
