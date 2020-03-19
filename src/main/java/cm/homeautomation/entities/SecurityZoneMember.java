package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class SecurityZoneMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JsonIgnore
	private SecurityZone securityZone;
	@OneToOne
	private Window window;

	private boolean violated;

	public Long getId() {
		return id;
	}

	@JsonIgnore
	public SecurityZone getSecurityZone() {
		return securityZone;
	}

	public Window getWindow() {
		return window;
	}

	public boolean isViolated() {
		return violated;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setSecurityZone(SecurityZone securityZone) {
		this.securityZone = securityZone;
	}

	public void setViolated(boolean violated) {
		this.violated = violated;
	}

	public void setWindow(Window window) {
		this.window = window;
	}

}
