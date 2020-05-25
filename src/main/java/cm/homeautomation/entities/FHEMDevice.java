
package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;



@Entity
public class FHEMDevice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	
	private FHEMDeviceType deviceType;
	private Long referencedId;
	private String name;
	

	public enum FHEMDeviceType{
		WINDOWBLIND, WINDOW, SWITCH, DEVICE
	}

	public FHEMDevice() {
		// do nothing
	}

	public void setId(Long id) {
		this.id = id;	
	}
	
	public Long getId() {
		return id;
	}

	public void setDeviceType(FHEMDeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
	public FHEMDeviceType getDeviceType() {
		return deviceType;
	}

	public void setReferencedId(Long referencedId) {
		this.referencedId = referencedId;
	}
	
	public Long getReferencedId() {
		return referencedId;
	}

	public void setName(String name) {
		this.name = name;
		
	}
	
	public String getName() {
		return name;
	}
	
}
