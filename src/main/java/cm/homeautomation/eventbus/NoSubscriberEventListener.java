package cm.homeautomation.eventbus;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.NoSubscriberEvent;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.services.base.AutoCreateInstance;
import io.quarkus.vertx.ConsumeEvent;

@AutoCreateInstance
public class NoSubscriberEventListener {

	public NoSubscriberEventListener() {
	}

	public void subscribe(NoSubscriberEvent event) {

		LogManager.getLogger(this.getClass())
				.error("no subscriber for event: " + event.originalEvent.getClass().getSimpleName());
	}
}
