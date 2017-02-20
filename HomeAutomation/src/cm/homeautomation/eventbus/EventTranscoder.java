package cm.homeautomation.eventbus;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
			e.printStackTrace();
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
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
