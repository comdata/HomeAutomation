package cm.homeautomation.entities;

import java.util.Date;

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

	@ManyToOne
	private NetworkDevice referencedNetworkDevice;

	private Date lastSeen = new Date();

	public String getActionEvent() {
		return actionEvent;
	}

	public Long getId() {
		return id;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public String getMac() {
		return mac;
	}

	public String getName() {
		return name;
	}

	public NetworkDevice getReferencedNetworkDevice() {
		return referencedNetworkDevice;
	}

	public Switch getReferencedSwitch() {
		return referencedSwitch;
	}

	public void setActionEvent(final String actionEvent) {
		this.actionEvent = actionEvent;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public void setMac(final String mac) {
		this.mac = mac;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setReferencedNetworkDevice(final NetworkDevice referencedNetworkDevice) {
		this.referencedNetworkDevice = referencedNetworkDevice;
	}

	public void setReferencedSwitch(final Switch referencedSwitch) {
		this.referencedSwitch = referencedSwitch;
	}
}
