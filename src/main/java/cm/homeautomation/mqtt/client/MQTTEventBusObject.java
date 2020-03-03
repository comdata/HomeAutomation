package cm.homeautomation.mqtt.client;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement
@JsonInclude (JsonInclude.Include.USE_DEFAULTS)
public class MQTTEventBusObject {

	private String topic;
	private String messageContent;

	public MQTTEventBusObject(String topic, String messageContent) {
		this.setTopic(topic);
		this.setMessageContent(messageContent);
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

}
