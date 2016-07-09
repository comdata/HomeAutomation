package cm.homeautomation.services.actor;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.eventbus.EventObject;

public class MessageTranscoder implements Encoder.Text<SwitchEvent>, Decoder.Text<SwitchEvent> {

	private ObjectMapper mapper;

	@Override
	public void init(EndpointConfig config) {
		mapper = new ObjectMapper();
	}

	@Override
	public void destroy() {

	}

	@Override
	public SwitchEvent decode(String s) throws DecodeException {
		try {
			return mapper.readValue(s, SwitchEvent.class);
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
	public String encode(SwitchEvent object) throws EncodeException {

		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
