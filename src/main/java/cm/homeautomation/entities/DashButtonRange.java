package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DashButtonRange {
	@Id
	@Column(name="VENDORRANGE")
	private String range=null;
	
	public void setRange(String range) {
		this.range = range;
	}
	
	public String getRange() {
		return range;
	}
}
