package cm.homeautomation.ebus;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class EBusMessageEvent implements HumanMessageGenerationInterface {

	private String topic;
	private String messageContent;

	public EBusMessageEvent(String topic, String messageContent) {
		this.topic = topic;
		this.messageContent = messageContent;

	}

	@Override
	public String getTitle() {

		return "EBUS";
	}

	@Override
	public String getMessageString() {
		return "Topic: " + topic + " message: " + messageContent;
	}

}
