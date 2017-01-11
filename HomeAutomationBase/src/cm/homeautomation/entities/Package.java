package cm.homeautomation.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Package {

	@EmbeddedId
	private PackagePK id;
	

	private String carrierFullName;
	private String packageName;
	private String dateAdded;
	private String dateModified;
	
	public PackagePK getId() {
		return id;
	}
	public void setId(PackagePK id) {
		this.id = id;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(String dateAdded) {
		this.dateAdded = dateAdded;
	}
	public String getDateModified() {
		return dateModified;
	}
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	
	
}
