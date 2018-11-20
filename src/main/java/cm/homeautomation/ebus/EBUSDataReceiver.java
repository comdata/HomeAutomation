package cm.homeautomation.ebus;

import org.apache.logging.log4j.LogManager;

public class EBUSDataReceiver {
	
	private EBUSDataReceiver() {
		// do nothing
	}

	public static void receiveEBUSData(String topic, String messageContent) {
		LogManager.getLogger(EBUSDataReceiver.class).debug("EBUS. Topic: "+ topic+" message: "+messageContent );
	}

}
