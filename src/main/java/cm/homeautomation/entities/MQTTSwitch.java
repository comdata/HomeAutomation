package cm.homeautomation.entities;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class MQTTSwitch extends Switch {
	private String mqttPowerOnTopic;
	private String mqttPowerOffTopic;
	private String mqttPowerOnMessage;
	private String mqttPowerOffMessage;
}
