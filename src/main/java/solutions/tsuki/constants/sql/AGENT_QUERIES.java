package solutions.tsuki.constants.sql;

public interface AGENT_QUERIES {
    String INSERT_AGENT_STATE_CHANGE_EVENT = "INSERT INTO agents_events(agent_id, state, event_timestamp) VALUES($1,$2,$3)";
}
