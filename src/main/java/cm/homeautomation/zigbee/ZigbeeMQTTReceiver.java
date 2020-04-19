package cm.homeautomation.zigbee;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicRecorder;
import cm.homeautomation.services.base.AutoCreateInstance;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AutoCreateInstance
public class ZigbeeMQTTReceiver {

    private static List<String> topicList=new ArrayList<>();
	private String zigbeeMqttTopic;

	public ZigbeeMQTTReceiver() {
		EventBusService.getEventBus().register(this);
		
		zigbeeMqttTopic = ConfigurationService.getConfigurationProperty("zigbee", "mqttTopic");
	}

	@Subscribe
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) {
		EntityManager em = EntityManagerService.getNewManager();

		@NonNull
		String topic = event.getTopic();
		log.debug(topic);

		if (topic.startsWith(zigbeeMqttTopic)) {
			// do zigbee magic
			LogManager.getLogger(this.getClass()).error("Got Zigbee message: "+event.getMessage());
		}
	}

}
