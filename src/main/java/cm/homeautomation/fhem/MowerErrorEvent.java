package cm.homeautomation.fhem;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class MowerErrorEvent implements HumanMessageGenerationInterface {

	
	private String errorMessage;

	public MowerErrorEvent(String errorMessage) {
		this.setErrorMessage(errorMessage);
	}
	
	@Override
	public String getTitle() {
	
		return "Mower error";
	}

	@Override
	public String getMessageString() {
		return getErrorMessage();
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
