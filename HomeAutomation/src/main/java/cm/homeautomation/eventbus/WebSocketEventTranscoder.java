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

import cm.homeautomation.logging.WebSocketEvent;


public class WebSocketEventTranscoder implements Encoder.Text<WebSocketEvent>, Decoder.Text<WebSocketEvent> {
	private ObjectMapper mapper;

	public WebSocketEventTranscoder() {
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
	public WebSocketEvent decode(String s) throws DecodeException {
		try {
			return mapper.readValue(s, WebSocketEvent.class);
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
	public String encode(WebSocketEvent object) throws EncodeException {

		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error("encoding failed.", e);
		}
		return null;
	}

}
