package cm.homeautomation.services.messaging;

import cm.homeautomation.eventbus.EventBusHumanMessageIgnore;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import io.quarkus.vertx.ConsumeEvent;

public class HumanMessageEventTranslator {

	public HumanMessageEventTranslator() {
		EventBusService.getEventBus().register(this);
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handleEvent(final EventObject eventObject) {

		final Object eventData = eventObject.getData();
		if (eventData instanceof HumanMessageGenerationInterface) {
			if (!eventData.getClass().isAnnotationPresent(EventBusHumanMessageIgnore.class)) {

				final HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventData;
				EventBusService.getEventBus()
						.post(new EventObject(new HumanMessageEvent(humanMessage.getMessageString())));
			}
		}

	}
}
