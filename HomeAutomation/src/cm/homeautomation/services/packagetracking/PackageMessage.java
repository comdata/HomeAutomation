package cm.homeautomation.services.packagetracking;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class PackageMessage {
	@JsonAnySetter
	public void handleUnknownProperties(String key, Object value) {
		Logger.getLogger(this.getClass()).info(key+"- "+value);
	}
}
