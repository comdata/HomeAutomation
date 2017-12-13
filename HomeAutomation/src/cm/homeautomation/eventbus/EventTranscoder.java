package cm.homeautomation.eventbus;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventTranscoder implements Encoder.Text<EventObject>, Decoder.Text<EventObject> {
	private ObjectMapper mapper;

	public EventTranscoder() {
		init(null);
	}

	@Override
	public EventObject decode(final String s) throws DecodeException {
		try {
			return mapper.readValue(s, EventObject.class);
		} catch (final IOException e) {
			LogManager.getLogger(this.getClass()).error("decoding failed: " + s, e);
		}
		return null;
	}

	@Override
	public void destroy() {

	}

	@Override
	public String encode(final EventObject object) throws EncodeException {

		try {
			final String writeValueAsString = mapper.writeValueAsString(object);
			LogManager.getLogger(this.getClass()).trace("encoded as: " + writeValueAsString);
			return writeValueAsString;
		} catch (final JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error("encoding failed.", e);
		}
		return null;
	}

	@Override
	public void init(final EndpointConfig config) {
		mapper = new ObjectMapper();
		// mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public boolean willDecode(final String s) {
		return true;
	}

}
