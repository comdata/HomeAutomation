package cm.homeautomation.mqtt.humanmessageemitter;

import java.util.List;

import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.HumanMessageEmitterFilter;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.mqtt.client.MQTTSendEvent;
import cm.homeautomation.services.base.AutoCreateInstance;
import io.quarkus.scheduler.Scheduled;

@AutoCreateInstance
public class MQTTHumanMessageEmitter {

	private String humanMessageTopic;
	private static List<HumanMessageEmitterFilter> filterList;

	public MQTTHumanMessageEmitter() {
		humanMessageTopic = ConfigurationService.getConfigurationProperty("mqtt", "humanMessageTopic");

		if (humanMessageTopic != null && !"".equals(humanMessageTopic)) {
			EventBusService.getEventBus().register(this);
		}
	}

	private boolean checkMessageFiltered(String message) {
		if (message != null) {
			for (HumanMessageEmitterFilter filter : filterList) {
				if (message.contains(filter.getMessagePart())) {
					return true;
				}
			}
		}

		return false;
	}

	@Scheduled(every = "120s")
	public void updateHumanMessageFilter() {
		EntityManager em = EntityManagerService.getManager();

		filterList = em.createQuery("select f from HumanMessageEmitterFilter f", HumanMessageEmitterFilter.class)
				.getResultList();
	}

	@Subscribe
	public void handleEvent(final EventObject eventObject) {
		Runnable eventThread = () -> {
			if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
				HumanMessageGenerationInterface humanMessageGenerationInterface = (HumanMessageGenerationInterface) eventObject
						.getData();
				String message = humanMessageGenerationInterface.getTitle() + ": "
						+ humanMessageGenerationInterface.getMessageString();

				boolean filtered = checkMessageFiltered(message);

				// message must not be set to ignore and not be filtered
				if (!filtered) {
					EventBusService.getEventBus().post(new MQTTSendEvent(humanMessageTopic, message));
				}
			}
		};
		new Thread(eventThread).start();
	}
}
