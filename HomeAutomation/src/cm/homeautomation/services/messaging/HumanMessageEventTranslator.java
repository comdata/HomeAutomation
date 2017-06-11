package cm.homeautomation.services.messaging;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.telegram.TelegramBotService;

public class HumanMessageEventTranslator {

	public HumanMessageEventTranslator() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleEvent(EventObject eventObject) {

		Object eventData = eventObject.getData();
		if (eventData instanceof HumanMessageGenerationInterface) {
			HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventData;
			EventBusService.getEventBus().post(new EventObject(new HumanMessageEvent(humanMessage.getMessageString())));
		}

	}
}
