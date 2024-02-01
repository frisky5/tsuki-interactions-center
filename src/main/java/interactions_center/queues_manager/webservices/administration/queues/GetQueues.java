package interactions_center.queues_manager.webservices.administration.queues;

import interactions_center.agents_manager.json.responses.GenericResponse;
import interactions_center.agents_manager.json.responses.Queue;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("/v1/queues_manager/admin/queues")
@NoCache
public class GetQueues {
    private final Logger LOG = LoggerFactory.getLogger(GetQueues.class);
    private final String SELECT_QUEUES = "SELECT * FROM queues_view";

    @Inject
    PgPool pgPool;

    @GET
    @Blocking
    @RolesAllowed({"admin:agent:view", "supervisor:agent:view"})
    public Response get() {
        GenericResponse response = new GenericResponse();
        List<Queue> queues = new ArrayList<>();
        pgPool.query(SELECT_QUEUES).executeAndAwait().iterator().forEachRemaining(row -> {
            Queue temp = new Queue();
            temp.setId(row.getUUID("id"));
            temp.setName(row.getString("name").concat(" ").concat("(").concat(row.getString("type_display_name").concat(")")));
            temp.setType(row.getInteger("type"));
            queues.add(temp);
        });
        response.setQueues(queues);
        return Response.ok(response).build();
    }
}
