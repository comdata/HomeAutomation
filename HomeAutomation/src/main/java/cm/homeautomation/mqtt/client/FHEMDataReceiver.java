package cm.homeautomation.mqtt.client;

/**
 * receive and event stream from FHEM via MQTT
 * 
 * @author christoph
 *
 */
public class FHEMDataReceiver {

	private FHEMDataReceiver() {
		// do nothing
	}
	
	public static void receiveFHEMData(String topic, String messageContent) {
System.out.println("FHEM message for topic: "+topic+" message: "+messageContent);
	}

}
