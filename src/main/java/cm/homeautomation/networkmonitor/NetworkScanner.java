package cm.homeautomation.networkmonitor;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.mqtt.client.MQTTSendEvent;
import io.quarkus.runtime.Startup;
import io.vertx.core.eventbus.EventBus;

@Startup
@Singleton
public class NetworkScanner {

	@Inject
	EventBus bus;

	private static NetworkScanner instance;

	public NetworkScanner() {
		instance = this;
	}

	/**
	 * make this a singleton
	 * 
	 * @return
	 */
	public static NetworkScanner getInstance() {
		return instance;
	}

	/**
	 * provide interface for the scheduler to scan the network for hosts
	 * 
	 * @param args
	 */
	public static void scanNetwork(String[] args) {
		try {
			instance.scanNetworkInternal(args);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void scanNetworkInternal(String[] args) throws JsonProcessingException {
		String subnet = args[0];
		
		NetworkScanEvent networkScanEvent = new NetworkScanEvent(subnet);
		
		String payload = (new ObjectMapper()).writeValueAsString(networkScanEvent);
		
		bus.publish("MQTTSendEvent", new MQTTSendEvent("networkServices/scan", payload));
		
	}


}
