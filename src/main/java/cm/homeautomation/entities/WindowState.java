package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


@Entity
public class WindowState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("window")
	@ManyToOne
	@JoinColumn(name = "WINDOW_ID")
	
	private Window window;

	private Date timestamp;
	private int state;
	private String mac;
	private String externalId;

	public Long getId() {
		return id;
	}

	public String getMac() {
		return mac;
	}

	public int getState() {
		return state;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}
}
