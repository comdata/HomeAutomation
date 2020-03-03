package cm.homeautomation.mqtt.topicrecorder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class MQTTTopicEvent {
	
	@NonNull
	private String topic;
	
	private String message;
}
