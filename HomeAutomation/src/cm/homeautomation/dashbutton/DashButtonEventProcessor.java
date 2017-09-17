package cm.homeautomation.dashbutton;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.eventbus.EventObject;

public class DashButtonEventProcessor {

	@Subscribe
	public void subscribe(EventObject eventObject) {
		Object data = eventObject.getData();
		if (data instanceof DashButtonEvent) {
			DashButtonEvent dbEvent=(DashButtonEvent)data;
			String mac = dbEvent.getMac();
			
			System.out.println("Dashbutton MAC: "+mac);
		}
	}
}
