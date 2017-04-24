package cm.homeautomation.sensors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RainData extends JSONSensorDataBase {
	private int rc;
	private int state;
	private String mac;
	
	public RainData() {
	
	}

	public int getRc() {
		return rc;
	}

	public void setRc(int rc) {
		this.rc = rc;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}
	
	
}
