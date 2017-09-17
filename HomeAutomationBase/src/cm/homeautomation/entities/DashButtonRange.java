package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DashButtonRange {
	@Id
	private String range=null;
	
	public void setRange(String range) {
		this.range = range;
	}
	
	public String getRange() {
		return range;
	}
}
