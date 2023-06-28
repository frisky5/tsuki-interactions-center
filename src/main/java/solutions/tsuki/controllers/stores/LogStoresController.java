package solutions.tsuki.controllers.stores;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import solutions.tsuki.stores.InteractionsStore;

@Path("/v1/stores/print")
public class LogStoresController {

    @Inject
    InteractionsStore interactionsStore;

    @GET
    public void print() {
        interactionsStore.printStoreToLog();
    }
}
