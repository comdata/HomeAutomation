package cm.homeautomation.mqtt.humanmessageemitter;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.entities.HumanMessageEmitterFilter;

import javax.persistence.EntityManager;
import java.util.List;
import cm.homeautomation.db.EntityManagerService;
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

		filterList = em.createQuery("select f from HumanMessageEmitterFilter f", HumanMessageEmitterFilter.class).getResultList();
	}

	@Subscribe
	public void handleEvent(final EventObject eventObject) {
		if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
			HumanMessageGenerationInterface humanMessageGenerationInterface = (HumanMessageGenerationInterface) eventObject
					.getData();
			String message = humanMessageGenerationInterface.getTitle() + ": "
                    + humanMessageGenerationInterface.getMessageString();
            
            boolean filtered = checkMessageFiltered(message);

			// message must not be set to ignore and not be filtered
			if (!filtered) {
                MQTTSender.sendMQTTMessage(humanMessageTopic, message);
            }
		}
	}
}
