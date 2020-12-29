package cm.homeautomation.networkmonitor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.mqtt.client.MQTTSendEvent;
import cm.homeautomation.services.scheduler.JobArguments;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Startup
@ApplicationScoped
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
	@ConsumeEvent(value = "NetworkScanner", blocking = true)
	public void scanNetworkInternal(JobArguments args) throws JsonProcessingException {
		String subnet = args.getArgumentList().get(0);
		
		NetworkScanEvent networkScanEvent = new NetworkScanEvent(subnet);
		
		String payload = (new ObjectMapper()).writeValueAsString(networkScanEvent);
		
		bus.publish("MQTTSendEvent", new MQTTSendEvent("networkServices/scan", payload));
		
	}


}
