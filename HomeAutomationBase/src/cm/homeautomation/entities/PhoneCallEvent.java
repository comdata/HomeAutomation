package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class PhoneCallEvent implements HumanMessageGenerationInterface {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Date timestamp=new Date();
	private String mode;
	private String internalNumber;
	private String externalNumber;
	private String event;

	public PhoneCallEvent(String event, String mode, String internalNumber, String externalNumber) {
		this.setEvent(event);
		this.setMode(mode);
		this.setInternalNumber(internalNumber);
		this.setExternalNumber(externalNumber);

	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getInternalNumber() {
		return internalNumber;
	}

	public void setInternalNumber(String internalNumber) {
		this.internalNumber = internalNumber;
	}

	public String getExternalNumber() {
		return externalNumber;
	}

	public void setExternalNumber(String externalNumber) {
		this.externalNumber = externalNumber;
	}

	@Override
	public String getMessageString() {

		return "Call event. Event: "+event+" Mode: " + mode + " external number: " + externalNumber;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
