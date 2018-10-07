package cm.homeautomation.eventbus;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class StringTranscoder implements Encoder.Text<String>, Decoder.Text<String> {

	@Override
	public void init(EndpointConfig endpointConfig) {
		
	}

	@Override
	public void destroy() {
	}

	@Override
	public String decode(String s) throws DecodeException {
		return s;
	}

	@Override
	public boolean willDecode(String s) {
		
		return true;
	}

	@Override
	public String encode(String object) throws EncodeException {
		return object;
	}

}
