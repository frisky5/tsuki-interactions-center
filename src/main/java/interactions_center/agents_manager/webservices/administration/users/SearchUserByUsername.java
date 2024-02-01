package interactions_center.agents_manager.webservices.administration.users;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestQuery;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.agents_manager.json.responses.GenericResponse;
import interactions_center.agents_manager.json.responses.User;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("/v1/agents_manager/admin/users/search")
@NoCache
public class SearchUserByUsername {
    private final Logger LOG = LoggerFactory.getLogger(SearchUserByUsername.class);

    @Inject
    Keycloak keycloak;

    @GET
    @RolesAllowed(value = {"ic_admin"})
    @Blocking
    public Uni<Response> get(@RestQuery("") String realm, @RestQuery("") String username) {
        return Uni.createFrom().nullItem().onItem().transform(item -> {
            GenericResponse response = new GenericResponse();
            if(realm.isBlank() || username.isBlank())
            {
                response.setError(true);
                response.setMessage("either realm or username query parameter is empty");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }
            try {
                List<UserRepresentation> usersSearchResult = keycloak.realm(realm).users().search(username, false);
                ArrayList<User> users = new ArrayList<>(usersSearchResult.size());
                usersSearchResult.iterator().forEachRemaining(user -> {
                    User temp = new User();
                    temp.setId(user.getId());
                    temp.setFirstName(user.getFirstName());
                    temp.setLastName(user.getLastName());
                    temp.setUsername(user.getUsername());
                    users.add(temp);
                });
                response.setUsers(users);
                return Response.ok(response).build();
            } catch (Exception e) {
                LOG.warn("search users by username failed", e);
                response.setError(true);
                response.setMessage("Failed to search Users by username");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
            }
        });
    }
}
