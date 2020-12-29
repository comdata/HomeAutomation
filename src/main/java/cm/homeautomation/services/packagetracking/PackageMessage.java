package cm.homeautomation.services.packagetracking;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class PackageMessage {
	@JsonAnySetter
	public void handleUnknownProperties(String key, Object value) {
		//LogManager.getLogger(this.getClass()).info(key+"- "+value);
	}
}
