package cm.homeautomation.services.overview;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OverviewMessageTranscoder implements Encoder.Text<OverviewTile>, Decoder.Text<OverviewTile> {

	private ObjectMapper mapper;

	@Override
	public void init(EndpointConfig config) {
		mapper = new ObjectMapper();
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public OverviewTile decode(String s) throws DecodeException {
		try {
			return mapper.readValue(s, OverviewTile.class);
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).error("IOException while decoding: "+s, e);
		}
		return null;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

	@Override
	public String encode(OverviewTile object) throws EncodeException {

		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error("JsonProcessingException while encoding.", e);
		}
		return null;
	}

}
