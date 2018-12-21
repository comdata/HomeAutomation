package cm.homeautomation.ebus;

import cm.homeautomation.eventbus.EventBusHumanMessageIgnore;
import cm.homeautomation.telegram.TelegramIgnore;

@EventBusHumanMessageIgnore
@TelegramIgnore
public class EBusMessageEvent {

	private static final String EBUS = "EBUS";
	private String topic;
	private String messageContent;

	public EBusMessageEvent(String topic, String messageContent) {
		this.setTopic(topic);
		this.setMessageContent(messageContent);

	}

	public String getTitle() {

		return EBUS;
	}

	public String getMessageString() {
		return "Topic: " + getTopic() + " message: " + getMessageContent();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof EBusMessageEvent) {
			EBusMessageEvent ebusMessageEvent = (EBusMessageEvent) obj;
			return (getTopic() != null && getTopic().equals(ebusMessageEvent.getTitle()) && getMessageString() != null
					&& getMessageString().equals(ebusMessageEvent.getMessageString()));
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getTopic().hashCode() + getMessageString().hashCode();
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
