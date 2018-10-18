package cm.homeautomation.transmission;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class TransmissionDownloadFinishedEvent implements HumanMessageGenerationInterface {

	private String name;

	public TransmissionDownloadFinishedEvent() {
		
	}

	public TransmissionDownloadFinishedEvent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getMessageString() {
		return "Download of: "+name+" finished.";
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Transmission Download Finished";
	}
}
