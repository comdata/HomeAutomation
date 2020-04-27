package cm.homeautomation.services.hueinterface;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HueEmulatorMessage {

	// {"on":true,"from":"::ffff:192.168.1.127","on_off_command":true,"payload":"on","change_direction":0,"bri":100,"bri_normalized":1,"device_name":"Rolläden
	// Wohnzimmer","light_id":"1a6d4e32544a02","port":36257,"_msgid":"9f807153.336d"}

	@JsonAlias("on_off_command")
	boolean onOffCommand;

	boolean on;

	// TODO xy als Array

	String payload;

	@JsonAlias("bri")
	int brightness;

	@JsonAlias("device_name")
	String deviceName;

	@JsonAlias("light_id")
	String lightId;

	HueColor color;
}
