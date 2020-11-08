package cm.homeautomation.services.messaging;

import javax.inject.Inject;
import javax.inject.Singleton;

import cm.homeautomation.eventbus.EventBusHumanMessageIgnore;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.services.actor.ActorPressSwitchEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Singleton
public class HumanMessageEventTranslator {
	
	@Inject
	EventBus bus;
	
	@ConsumeEvent(value = "RemoteControlEvent", blocking = true)
	public void subscribe(RemoteControlEvent event) {
		bus.publish("EventObject", new EventObject(new HumanMessageEvent("Remote Control "+event.getName()+" "+event.getEventType().toString()+" "+event.isPoweredOnState())));
	}
	
	@ConsumeEvent(value = "ActorPressSwitchEvent", blocking = true)
	public void handleEvent(final ActorPressSwitchEvent eventObject) {
		bus.publish("EventObject", new EventObject(new HumanMessageEvent("Pressing "+eventObject.getSwitchId()+ " to "+eventObject.getTargetStatus())));

	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handleEvent(final EventObject eventObject) {

		final Object eventData = eventObject.getData();
		if (eventData instanceof HumanMessageGenerationInterface) {
			if (!eventData.getClass().isAnnotationPresent(EventBusHumanMessageIgnore.class)) {

				final HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventData;
				bus.publish("HumanMessageEvent", new HumanMessageEvent(humanMessage.getTitle() + ": "
						+ humanMessage.getMessageString()));
			}
		}

	}
}
