package solutions.tsuki.constants.sql.agent;

public interface AGENT_QUERIES {
    String INSERT_AGENT_STATE_EVENT = "INSERT INTO agents_events(agent_id, state, event_timestamp) VALUES($1,$2,$3)";
}
