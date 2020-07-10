package cm.homeautomation.eventbus;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.NoSubscriberEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class NoSubscriberEventListener {

	public NoSubscriberEventListener() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void subscribe(NoSubscriberEvent event) {

		LogManager.getLogger(this.getClass())
				.error("no subscriber for event: " + event.originalEvent.getClass().getSimpleName());
	}
}
