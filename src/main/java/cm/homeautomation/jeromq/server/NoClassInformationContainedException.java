package cm.homeautomation.jeromq.server;

public class NoClassInformationContainedException extends Exception {

	private String messageContent;

	public NoClassInformationContainedException(String messageContent) {
		this.messageContent = messageContent;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5308104200991702914L;
	
	@Override
	public String getMessage() {
		return super.getMessage()+" messageContent: "+messageContent;
	}

}
