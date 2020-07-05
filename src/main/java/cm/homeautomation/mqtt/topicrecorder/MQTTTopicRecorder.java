package cm.homeautomation.mqtt.topicrecorder;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.eventbus.EventBusService;
import io.quarkus.runtime.Startup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * record a list of all seen MQTT topics
 * 
 * @author christoph
 *
 */
@Startup
@ApplicationScoped
@Log4j2
public class MQTTTopicRecorder {

    private static List<String> topicList=new ArrayList<>();

	public MQTTTopicRecorder() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) {
		EntityManager em = EntityManagerService.getManager();

		@NonNull
		String topic = event.getTopic();
		log.debug(topic);

        if (!topicList.contains(topic)) {
            List<MQTTTopic> mqttTopicList = em.createQuery("select t from MQTTTopic t where t.topic=:topic", MQTTTopic.class)
                    .setParameter("topic", topic).getResultList();

            if (mqttTopicList == null || mqttTopicList.isEmpty()) {
                em.getTransaction().begin();
                MQTTTopic mqttTopic = new MQTTTopic(topic);

                em.persist(mqttTopic);
                em.getTransaction().commit();
                log.debug("MQTT topic created. Topic: " + topic);
            }

            topicList.add(topic);

        }



	}

}
