package cm.homeautomation.zigbee.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ZigBeeTradfriRemoteControl {
	@Id
	String ieeeAddr;
	
	// powered on?
	boolean powerOnState=false;
	
	
}
