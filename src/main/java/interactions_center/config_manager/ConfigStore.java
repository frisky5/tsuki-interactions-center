package interactions_center.config_manager;

import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ConfigStore {
    private final Logger LOG = LoggerFactory.getLogger(ConfigStore.class);
    private final String SELECT_CONFIG_CONFIGURATION = "SELECT * FROM config_configuration";
    private final ConcurrentHashMap<String, String> config = new ConcurrentHashMap<>(15);
    private PgPool pgClient;

    public ConfigStore(PgPool pgClient) {
        this.pgClient = pgClient;
        this.pgClient.query(SELECT_CONFIG_CONFIGURATION).executeAndAwait().iterator().forEachRemaining(row -> {
            LOG.info("found config map of key [{}] with value [{}]", row.getString("key"), row.getString("value"));
            config.put(row.getString("key"), row.getString("value"));
        });
    }

    public String get(String key) {
        return config.get(key);
    }

}
