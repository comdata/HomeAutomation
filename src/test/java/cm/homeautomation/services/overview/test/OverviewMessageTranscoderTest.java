package cm.homeautomation.services.overview.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.entities.Room;
import cm.homeautomation.services.overview.OverviewMessageTranscoder;
import cm.homeautomation.services.overview.OverviewTile;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;

public class OverviewMessageTranscoderTest {
	
	@Tested OverviewMessageTranscoder transcoder;
	@Mocked ObjectMapper mapper;
	private OverviewMessageTranscoder overviewMessageTranscoder;

	
	@BeforeEach
	private void setup() {
		overviewMessageTranscoder = new OverviewMessageTranscoder();
	}
	
	@Test
	public void testInit() {
		assertNotNull(overviewMessageTranscoder.getMapper());
	}
	
	@Test
	public void testDestroy() {
		overviewMessageTranscoder.destroy();
	}
	
	@Test
	public void testDecode() throws DecodeException, IOException {
		String stringToDecode = "{\"roomId\": 1}";
		OverviewTile expectedTile = new OverviewTile();
		expectedTile.setRoomId("1");
		
        new Expectations() {{
        	mapper.readValue(stringToDecode, OverviewTile.class);
            result = expectedTile;
        }};
		
		assertNotNull(overviewMessageTranscoder.getMapper());
		
		OverviewTile decode = overviewMessageTranscoder.decode("{\"roomId\": 1}");
		
		assertNotNull(decode);
		assertEquals("1", decode.getRoomId());
	}
	

	@Test
	public void testDecodeIOException() throws DecodeException,IOException {
		
		String stringToDecode = "{\"roomId\": 1}";
        new Expectations() {{
        	mapper.readValue(stringToDecode, OverviewTile.class);
            result = new IOException();;
        }};
		
		assertNotNull(overviewMessageTranscoder.getMapper());
		
		OverviewTile decode = overviewMessageTranscoder.decode(stringToDecode);
		
		assertNull(decode);
	}
	
	@Test
	public void testEncode() throws DecodeException, EncodeException, JsonProcessingException {
		OverviewTile object = new OverviewTile();
		object.setRoomId("1");
		String expected = "{\"roomId\":\"1\",\"icon\":null,\"number\":null,\"numberUnit\":null,\"title\":null,\"info\":null,\"infoState\":null,\"roomName\":null,\"eventHandler\":\"handleSelect\",\"tileType\":null}";

        new Expectations() {{
        	mapper.writeValueAsString(object);
            result = expected;
        }};
		
		assertNotNull(overviewMessageTranscoder.getMapper());
		

		String encoded = overviewMessageTranscoder.encode(object);
		
		assertNotNull(encoded);
		assertEquals(expected,encoded );
	}
	
	@Test
	public void testEncodeWithException() throws DecodeException, EncodeException, JsonProcessingException {
		OverviewTile object = new OverviewTile();
		object.setRoomId("1");
		
        new Expectations() {{
        	mapper.writeValueAsString(object);
            result = new JsonParseException(null, "failed");
        }};
		
		assertNotNull(overviewMessageTranscoder.getMapper());
		

		String encoded = overviewMessageTranscoder.encode(object);
		
		assertNull(encoded);
	
	}
	
	


	@Test
	public void testWillDecode() {
		assertTrue(overviewMessageTranscoder.willDecode(""));
	}
	
}
