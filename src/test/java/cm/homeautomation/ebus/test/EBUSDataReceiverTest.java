package cm.homeautomation.ebus.test;

import org.apache.logging.log4j.core.Logger;
import org.junit.Test;

import cm.homeautomation.ebus.EBUSDataReceiver;
import cm.homeautomation.ebus.EBusMessageEvent;
import cm.homeautomation.eventbus.CustomEventBus;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Mocked;

public class EBUSDataReceiverTest {

	@Test
	public void testReceiveEBUSData(@Capturing Logger logger) throws Exception {
		String messageContent = "123";
		String topic = "ebusd/test";
		String message = "EBUS. Topic: " + topic + " message: " + messageContent;
		new Expectations() {
			{
				logger.debug(message);
			}
		};

		// default test
		EBUSDataReceiver.receiveEBUSData(topic, messageContent);
	}
	
	@Test
	public void testReceiveStatus01FlameOff(@Capturing Logger logger, @Capturing CustomEventBus eventbus, @Mocked EBusMessageEvent ebusMessageEvent ) {
		String messageContent="37.0;36.0;2.250;36.0;39.0;off";
		String topic="ebusd/bai/Status01";
		
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