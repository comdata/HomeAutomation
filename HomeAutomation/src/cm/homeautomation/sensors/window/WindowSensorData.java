package cm.homeautomation.sensors.window;

import cm.homeautomation.entities.Room;
import cm.homeautomation.sensors.JSONSensorDataBase;

public class WindowSensorData extends JSONSensorDataBase {

	private int state;
	private String mac;


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
