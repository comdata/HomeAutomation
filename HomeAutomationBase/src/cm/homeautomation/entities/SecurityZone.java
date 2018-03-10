package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class SecurityZone {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany
	private List<SecurityZoneMember> zoneMembers;
	
	private boolean state;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SecurityZoneMember> getZoneMembers() {
		return zoneMembers;
	}

	public void setZoneMembers(List<SecurityZoneMember> zoneMembers) {
		this.zoneMembers = zoneMembers;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

}
