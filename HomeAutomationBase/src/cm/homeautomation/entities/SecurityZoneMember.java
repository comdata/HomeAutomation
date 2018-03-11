package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class SecurityZoneMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private SecurityZone securityZone;
	@OneToOne
	private Window window;

	public Long getId() {
		return id;
	}

	public SecurityZone getSecurityZone() {
		return securityZone;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setSecurityZone(SecurityZone securityZone) {
		this.securityZone = securityZone;
	}

	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}

}
