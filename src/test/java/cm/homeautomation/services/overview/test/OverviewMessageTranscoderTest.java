package cm.homeautomation.services.overview.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.services.overview.OverviewMessageTranscoder;
import cm.homeautomation.services.overview.OverviewTile;

public class OverviewMessageTranscoderTest {

	private OverviewMessageTranscoder overviewMessageTranscoder;

	@Test
	public void testInit() {
		assertNotNull(overviewMessageTranscoder.getMapper());
	}
	
	@Test
	public void testDestroy() {
		overviewMessageTranscoder.destroy();
	}
	
	@Test
	public void testDecode() throws DecodeException {
		assertNotNull(overviewMessageTranscoder.getMapper());
		
		OverviewTile decode = overviewMessageTranscoder.decode("{\"roomId\": 1}");
		
		assertNotNull(decode);
		assertEquals(decode.getRoomId(), "1");
	}
	
	@Test
	public void testEncode() throws DecodeException, EncodeException {
		assertNotNull(overviewMessageTranscoder.getMapper());
		
		OverviewTile object = new OverviewTile();
		object.setRoomId("1");
		String encoded = overviewMessageTranscoder.encode(object);
		
		assertNotNull(encoded);
		assertEquals(encoded, "{\"roomId\":\"1\",\"icon\":null,\"number\":null,\"numberUnit\":null,\"title\":null,\"info\":null,\"infoState\":null,\"roomName\":null,\"eventHandler\":\"handleSelect\",\"tileType\":null}" );
	}

	@BeforeEach
	private void setup() {
		overviewMessageTranscoder = new OverviewMessageTranscoder();
	}

	@Test
	public void testWillDecode() {
		assertTrue(overviewMessageTranscoder.willDecode(""));
	}
	
}
