package cm.homeautomation.mqtt.humanmessageemitter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.HumanMessageEmitterFilter;
import cm.homeautomation.mqtt.client.MQTTSendEvent;
import cm.homeautomation.services.messaging.HumanMessageEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Singleton
public class MQTTHumanMessageEmitter {

	private String humanMessageTopic;
	private static List<HumanMessageEmitterFilter> filterList;
	
	@Inject
	EventBus bus;

	public MQTTHumanMessageEmitter() {
		humanMessageTopic = ConfigurationService.getConfigurationProperty("mqtt", "humanMessageTopic");

		if (humanMessageTopic != null && !"".equals(humanMessageTopic)) {
			
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

	@ConsumeEvent(value="HumanMessageEvent", blocking=true)
	public void handleEvent(final HumanMessageEvent eventObject) {
		Runnable eventThread = () -> {
			
				String message = eventObject.getMessage();

				boolean filtered = checkMessageFiltered(message);

				// message must not be set to ignore and not be filtered
				if (!filtered) {
					bus.publish("MQTTSendEvent", new MQTTSendEvent(humanMessageTopic, message));
				}
	
		};
		new Thread(eventThread).start();
	}
}
