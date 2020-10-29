package cm.homeautomation.mqtt.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MQTTSendEvent {
	private String topic;
	private String payload;
}