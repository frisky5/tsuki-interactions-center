package interactions_center.agents_manager.websockets.decoders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import interactions_center.agents_manager.json.requests.websocket.WsMessage;

public class JsonDecoder implements Decoder.Text<WsMessage> {
    private final Logger LOG = LoggerFactory.getLogger(JsonDecoder.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WsMessage decode(String s) throws DecodeException {
        try {
            return objectMapper.readValue(s, WsMessage.class);
        } catch (JsonProcessingException e) {
            return new WsMessage();
        }
    }

    @Override
    public boolean willDecode(String s) {
        try {
            objectMapper.readValue(s, WsMessage.class);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    @Override
    public void init(EndpointConfig config) {
        LOG.info("init called");
        Text.super.init(config);
    }

    @Override
    public void destroy() {
        LOG.info("destroy called");
        Text.super.destroy();
    }
}
