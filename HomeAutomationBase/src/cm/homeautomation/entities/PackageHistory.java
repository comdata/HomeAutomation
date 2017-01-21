package cm.homeautomation.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class PackageHistory {

	@EmbeddedId
	private PackageHistoryPK id;


	private String locationText;

	public PackageHistoryPK getId() {
		return id;
	}

	public void setId(PackageHistoryPK id) {
		this.id = id;
	}


	public void setLocationText(String locationText) {
		this.locationText = locationText;
	}

	public String getLocationText() {
		return locationText;
	}

}
