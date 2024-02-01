package interactions_center.agents_manager.webservices.security;

import interactions_center.agents_manager.json.requests.websocket.ValidateWsId;
import interactions_center.agents_manager.json.responses.AgentsGenericResponse;
import interactions_center.agents_manager.stores.AgentsStore;
import interactions_center.agents_manager.stores.AgtsWsSessionsStore;
import interactions_center.agents_manager.stores.items.Agent;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@ApplicationScoped
@Path("/v1/agents_manager/websocket/validate")
@NoCache
public class ValidateAgtWs {
    private final Logger LOG = LoggerFactory.getLogger(ValidateAgtWs.class);

    @Inject
    JsonWebToken accessToken;
    @Inject
    AgentsStore agentsStore;
    @Inject
    AgtsWsSessionsStore agtsWsSessionsStore;

    @POST
    @Blocking
    @RolesAllowed("agent")
    public Uni<RestResponse<AgentsGenericResponse>> validate(ValidateWsId requestBody) {
        return Uni.createFrom().nullItem().onItem().transform(request -> {
            AgentsGenericResponse response = new AgentsGenericResponse();
            Agent agent = agentsStore.get(accessToken.getSubject());

            if (Objects.isNull(agent)) {
                response.setError(true);
                response.setMessage("invalid agent uuid");
                return RestResponse.status(RestResponse.Status.UNAUTHORIZED,response);
            }
            try {
                agent.lock();
                LOG.info("LOCKED agent [{}]", agent.getId());
                int result = agtsWsSessionsStore.mapAgtUuidToSession(agent.getId(), requestBody.getWsId());

                if (result == -1) {
                    response.setError(true);
                    response.setCode(-1);
                    response.setMessage("provided session id doesn't exist");
                    return RestResponse.status(RestResponse.Status.UNAUTHORIZED,response);
                }

                return RestResponse.status(RestResponse.Status.OK,response);
            } finally {
                agent.unlock();
                LOG.info("UNLOCKED agent [{}]", agent.getId());
            }
        });
    }
}
