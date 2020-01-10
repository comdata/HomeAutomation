package cm.homeautomation.mqtt.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class MQTTTopicEvent {
	
	@NonNull
	private String topic;
}
