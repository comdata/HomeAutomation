package cm.homeautomation.dashbutton;

public class DashButtonEvent {

	private String mac;

	public DashButtonEvent(String mac) {
		this.setMac(mac);
		
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

}
