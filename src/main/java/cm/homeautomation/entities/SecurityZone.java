package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * SecurityZones provide the ability to build a custom security solution out of
 * it
 *
 * @author christoph
 *
 */
@Entity
public class SecurityZone {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	/*
	 * elements that make up a security zone
	 */
	@OneToMany
	private List<SecurityZoneMember> zoneMembers;

	/*
	 * scripting entity to be called in case of security zone violations
	 */
	@OneToOne
	private ScriptingEntity scriptingEntity;

	private boolean state;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ScriptingEntity getScriptingEntity() {
		return scriptingEntity;
	}

	public List<SecurityZoneMember> getZoneMembers() {
		return zoneMembers;
	}

	public boolean isState() {
		return state;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setScriptingEntity(ScriptingEntity scriptingEntity) {
		this.scriptingEntity = scriptingEntity;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public void setZoneMembers(List<SecurityZoneMember> zoneMembers) {
		this.zoneMembers = zoneMembers;
	}

}
