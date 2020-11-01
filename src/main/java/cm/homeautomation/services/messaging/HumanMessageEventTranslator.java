package cm.homeautomation.services.messaging;

import javax.inject.Inject;
import javax.inject.Singleton;

import cm.homeautomation.eventbus.EventBusHumanMessageIgnore;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Singleton
public class HumanMessageEventTranslator {
	
	@Inject
	EventBus bus;

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handleEvent(final EventObject eventObject) {

		final Object eventData = eventObject.getData();
		if (eventData instanceof HumanMessageGenerationInterface) {
			if (!eventData.getClass().isAnnotationPresent(EventBusHumanMessageIgnore.class)) {

				final HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventData;
				bus.publish("EventObject", new EventObject(new HumanMessageEvent(humanMessage.getMessageString())));
			}
		}

	}
}
