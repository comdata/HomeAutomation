package cm.homeautomation.services.packagetracking;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class PackageMessage {
	@JsonAnySetter
	public void handleUnknownProperties(String key, Object value) {
	    System.out.println(key+"- "+value);
	}
}
