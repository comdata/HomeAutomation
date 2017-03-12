package cm.homeautomation.services.tv;

import com.google.common.eventbus.Subscribe;
import com.homeautomation.tv.panasonic.PanasonicTVBinding;
import com.homeautomation.tv.panasonic.TVNotReachableException;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

public class TVService {
	
	
	private PanasonicTVBinding tvBinding;

	public TVService() {
		tvBinding = new PanasonicTVBinding();
		
		System.out.println("Tv IP: "+tvIp);
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void phoneEventHandler(EventObject eventObject) {
		
		Object eventData = eventObject.getData();
		
		
		if (eventData instanceof PhoneCallEvent) {
			
			PhoneCallEvent callEvent=(PhoneCallEvent)eventData;
			
			String tvIp= ConfigurationService.getConfigurationProperty("tv", "tvIp");
			
			String event = callEvent.getEvent();
			
			if ("ring".equals(event)) {
				try {
					tvBinding.sendCommand(tvIp, "MUTE");
				} catch (TVNotReachableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if ("disconnect".equals(event)) {
				try {
					tvBinding.sendCommand(tvIp, "MUTE");
				} catch (TVNotReachableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
