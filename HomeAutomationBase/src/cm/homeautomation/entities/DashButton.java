package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DashButton {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String mac;
	private String actionEvent;
	private String name;
	
	@ManyToOne
	private Switch referencedSwitch;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getActionEvent() {
		return actionEvent;
	}
	public void setActionEvent(String actionEvent) {
		this.actionEvent = actionEvent;
	}
	public Switch getReferencedSwitch() {
		return referencedSwitch;
	}
	public void setReferencedSwitch(Switch referencedSwitch) {
		this.referencedSwitch = referencedSwitch;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
