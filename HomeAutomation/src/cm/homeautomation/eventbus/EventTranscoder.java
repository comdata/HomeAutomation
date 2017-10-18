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
	public void init(EndpointConfig config) {
		mapper = new ObjectMapper();
		//mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public void destroy() {

	}

	@Override
	public EventObject decode(String s) throws DecodeException {
		try {
			return mapper.readValue(s, EventObject.class);
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).error("decoding failed: "+s, e);
		}
		return null;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

	@Override
	public String encode(EventObject object) throws EncodeException {

		try {
			String writeValueAsString = mapper.writeValueAsString(object);
			LogManager.getLogger(this.getClass()).info("encoded as: "+writeValueAsString);
			return writeValueAsString;
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error("encoding failed.", e);
		}
		return null;
	}

}
