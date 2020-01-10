package cm.homeautomation.mqtt.topicrecorder;

import java.util.List;

import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.services.base.AutoCreateInstance;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * record a list of all seen MQTT topics
 * 
 * @author christoph
 *
 */
@Log4j2
@AutoCreateInstance
public class MQTTTopicRecorder {

	public MQTTTopicRecorder() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) {
		EntityManager em = EntityManagerService.getNewManager();

		@NonNull
		String topic = event.getTopic();
		log.debug(topic);

		List<MQTTTopic> mqttTopicList = em.createQuery("select t from MQTTTopic t where t.topic=:topic", MQTTTopic.class)
				.setParameter("topic", topic).getResultList();

		if (mqttTopicList == null || mqttTopicList.isEmpty()) {
			em.getTransaction().begin();
			MQTTTopic mqttTopic = new MQTTTopic(topic);

			em.persist(mqttTopic);
			em.getTransaction().commit();
			log.debug("MQTT topic created. Topic: " + topic);
		}

	}

}
