package cm.homeautomation.ebus.test;

import org.apache.logging.log4j.core.Logger;
import org.junit.Test;

import cm.homeautomation.ebus.EBUSDataReceiver;
import mockit.Capturing;
import mockit.Expectations;

public class EBUSDataReceiverTest {

	@Test
	public void testReceiveEBUSData(@Capturing Logger logger) throws Exception {
		String messageContent = "123";
		String topic = "/ebusd/test";
		String message = "EBUS. Topic: " + topic + " message: " + messageContent;
		new Expectations() {
			{
				logger.debug(message);
			}
		};

		// default test
		EBUSDataReceiver.receiveEBUSData(topic, messageContent);
	}
}