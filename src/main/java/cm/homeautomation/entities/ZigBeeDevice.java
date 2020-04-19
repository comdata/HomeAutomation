package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ZigBeeDevice {

	// {"ieeeAddr":"0x00124b0018e27f02","type":"Coordinator","networkAddress":0,
	// "friendly_name":"Coordinator","softwareBuildID":"zStack12","dateCode":"20190608","lastSeen":1587283920055}}

	@Id
	String ieeeAddr;
	String type;
	int networkAddress;
	String model;
	String vendor;
	String description;
	String manufacturerID;
	String manufacturerName;
	String powerSource;
	String modelID;
	int hardwareVersion;
	

	@JsonAlias("friendly_name")
	String friendlyName;
	String softwareBuildID;
	String dateCode;
	Date lastSeen;

}
