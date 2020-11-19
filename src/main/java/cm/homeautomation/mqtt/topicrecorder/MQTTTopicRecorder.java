package cm.homeautomation.mqtt.topicrecorder;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.MQTTTopic;
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

	@Inject
	EntityManager em;
	
	@Inject
	ConfigurationService configurationService;
	
	private static List<String> topicList = new ArrayList<>();

	void startup(@Observes StartupEvent event) {
		initTopicMap();

	}

	@ConsumeEvent(value = "MQTTTopicEvent", blocking = true)
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) {

		@NonNull
		String topic = event.getTopic();
		log.debug(topic);

		if (!topicList.contains(topic)) {

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

		List<MQTTTopic> mqttTopicList = em.createQuery("select t from MQTTTopic t ", MQTTTopic.class).getResultList();
		List<String> topicListTemp = new ArrayList<>();

		for (MQTTTopic mqttTopic : mqttTopicList) {
			topicListTemp.add(mqttTopic.getTopic());
		}
		topicList = topicListTemp;
	}

}
