package cm.homeautomation.services.trips;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TripsUpdateEvent {

	private boolean updated=true;

	public TripsUpdateEvent() {
		this.updated=true;
	}
	
	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
	
	
}
