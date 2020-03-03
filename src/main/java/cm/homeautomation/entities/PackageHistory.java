package cm.homeautomation.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@XmlRootElement
public class PackageHistory {

	@EmbeddedId
	private PackageHistoryPK id;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("package")
	@JoinColumns({
			@JoinColumn(updatable = false, insertable = false, name = "carrier", referencedColumnName = "carrier"),
			@JoinColumn(updatable = false, insertable = false, name = "trackingNumber", referencedColumnName = "trackingNumber") })
	@ManyToOne
	private Package trackedPackage;

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

	@XmlTransient
	@JsonIgnore
	@JsonBackReference("package")
	public Package getTrackedPackage() {
		return trackedPackage;
	}

	public void setTrackedPackage(Package trackedPackage) {
		this.trackedPackage = trackedPackage;
	}

}
