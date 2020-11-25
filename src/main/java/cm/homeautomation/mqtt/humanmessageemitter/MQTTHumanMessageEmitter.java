package cm.homeautomation.mqtt.humanmessageemitter;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.eclipse.microprofile.context.ManagedExecutor;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.HumanMessageEmitterFilter;
import cm.homeautomation.mqtt.client.MQTTSendEvent;
import cm.homeautomation.services.messaging.HumanMessageEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
@Transactional(value = TxType.REQUIRES_NEW)
public class MQTTHumanMessageEmitter {
	private static List<HumanMessageEmitterFilter> filterList;

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	@Inject
	ManagedExecutor executor;

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

		filterList = em.createQuery("select f from HumanMessageEmitterFilter f", HumanMessageEmitterFilter.class)
				.getResultList();
	}

	@ConsumeEvent(value = "HumanMessageEvent", blocking = true)
	public void handleEvent(final HumanMessageEvent eventObject) {
		Runnable eventThread = () -> {

			String humanMessageTopic = configurationService.getConfigurationProperty("mqtt", "humanMessageTopic");

			String message = eventObject.getMessage();

			boolean filtered = checkMessageFiltered(message);

			// message must not be set to ignore and not be filtered
			if (!filtered) {
				bus.publish("MQTTSendEvent", new MQTTSendEvent(humanMessageTopic, message));
			}

		};
		executor.runAsync(eventThread);
	}
}
