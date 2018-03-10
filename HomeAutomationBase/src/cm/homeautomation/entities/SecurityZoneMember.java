package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class SecurityZoneMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private SecurityZone securityZone;

	@OneToMany
	private List<Window> windows;

	public Long getId() {
		return id;
	}

	public SecurityZone getSecurityZone() {
		return securityZone;
	}

	public List<Window> getWindows() {
		return windows;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setSecurityZone(SecurityZone securityZone) {
		this.securityZone = securityZone;
	}

	public void setWindows(List<Window> windows) {
		this.windows = windows;
	}
}
