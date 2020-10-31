package cm.homeautomation.mqtt.topicrecorder;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.eventbus.EventBusService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * record a list of all seen MQTT topics
 * 
 * @author christoph
 *
 */
@Log4j2
@Singleton
public class MQTTTopicRecorder {

	private static List<String> topicList = new ArrayList<>();

	void startup(@Observes StartupEvent event) {
		initTopicMap();
	//	EventBusService.getEventBus().register(this);
	}

	@ConsumeEvent(value="MQTTTopicEvent", blocking = true)
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) {

		@NonNull
		String topic = event.getTopic();
		log.debug(topic);

		if (!topicList.contains(topic)) {
			EntityManager em = EntityManagerService.getManager();

			em.getTransaction().begin();
			MQTTTopic mqttTopic = new MQTTTopic(topic);

			em.persist(mqttTopic);
			em.getTransaction().commit();
			log.debug("MQTT topic created. Topic: " + topic);

			topicList.add(topic);

		}

	}

	@Scheduled(every = "120s")
	public void initTopicMap() {
		final EntityManager em = EntityManagerService.getManager();

		List<MQTTTopic> mqttTopicList = em.createQuery("select t from MQTTTopic t ", MQTTTopic.class).getResultList();
		List<String> topicListTemp = new ArrayList<>();

		for (MQTTTopic mqttTopic : mqttTopicList) {
			topicListTemp.add(mqttTopic.getTopic());
		}
		topicList = topicListTemp;
	}

}
