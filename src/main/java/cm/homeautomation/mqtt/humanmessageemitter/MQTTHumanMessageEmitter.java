package cm.homeautomation.mqtt.humanmessageemitter;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.HumanMessageEmitterFilter;
import cm.homeautomation.mqtt.client.MQTTSendEvent;
import cm.homeautomation.services.messaging.HumanMessageEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class MQTTHumanMessageEmitter {
	private static List<HumanMessageEmitterFilter> filterList;

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	@Inject
	UserTransaction transaction;

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
			try {
				boolean ownTransaction = false;
				if (transaction.getStatus() == Status.STATUS_NO_TRANSACTION) {
					transaction.begin();
					ownTransaction = true;
				}

				String humanMessageTopic = configurationService.getConfigurationProperty("mqtt", "humanMessageTopic");

				String message = eventObject.getMessage();

				boolean filtered = checkMessageFiltered(message);

				// message must not be set to ignore and not be filtered
				if (!filtered) {
					bus.publish("MQTTSendEvent", new MQTTSendEvent(humanMessageTopic, message));
				}
				if (ownTransaction) {
					transaction.commit();
				}
			} catch (NotSupportedException | SystemException | SecurityException | IllegalStateException
					| RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		};
		new Thread(eventThread).start();
	}
}
