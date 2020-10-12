package cm.homeautomation.mqtt.humanmessageemitter;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class MQTTHumanMessageEmitter {

	private String humanMessageTopic;

	public MQTTHumanMessageEmitter() {
		humanMessageTopic = ConfigurationService.getConfigurationProperty("mqtt", "humanMessageTopic");

		if (humanMessageTopic != null && !"".equals(humanMessageTopic)) {
			EventBusService.getEventBus().register(this);
		}
	}

	@Subscribe
	public void handleEvent(final EventObject eventObject) {
		if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
			HumanMessageGenerationInterface humanMessageGenerationInterface = (HumanMessageGenerationInterface) eventObject
					.getData();
			String message = humanMessageGenerationInterface.getTitle() + ": "
					+ humanMessageGenerationInterface.getMessageString();
			MQTTSender.sendMQTTMessage(humanMessageTopic, message);
		}
	}
}
