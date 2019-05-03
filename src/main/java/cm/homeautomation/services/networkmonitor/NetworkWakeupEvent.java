package cm.homeautomation.services.networkmonitor;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class NetworkWakeupEvent implements HumanMessageGenerationInterface{

	private String mac;

	private NetworkWakeupEvent() {
		// do nothing
	}
	
	public NetworkWakeupEvent(String mac) {
		this.mac = mac;
		
	}
	@Override
	public String getTitle() {

		return "Network Wakeup";
	}

	@Override
	public String getMessageString() {
		
		return "Device with MAC: "+getMac()+" woken up.";
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

}
