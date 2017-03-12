package cm.homeautomation.services.phone;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.HumanMessageGenerationInterface;

@XmlRootElement
public class PhoneCallEvent implements HumanMessageGenerationInterface {

	private String mode;
	private String internalNumber;
	private String externalNumber;

	public PhoneCallEvent(String mode, String internalNumber, String externalNumber) {
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

		return "Call event. Mode: " + mode + " external number: " + externalNumber;
	}

}
