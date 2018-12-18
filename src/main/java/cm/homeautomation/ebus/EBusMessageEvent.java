package cm.homeautomation.ebus;

import cm.homeautomation.eventbus.EventBusHumanMessageIgnore;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.telegram.TelegramIgnore;

@EventBusHumanMessageIgnore
@TelegramIgnore
public class EBusMessageEvent implements HumanMessageGenerationInterface {

	private String topic;
	private String messageContent;

	public EBusMessageEvent(String topic, String messageContent) {
		this.setTopic(topic);
		this.messageContent = messageContent;

	}

	@Override
	public String getTitle() {

		return "EBUS";
	}

	@Override
	public String getMessageString() {
		return "Topic: " + getTopic() + " message: " + messageContent;
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
}
