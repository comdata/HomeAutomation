package cm.homeautomation.sensors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import cm.homeautomation.entities.Room;

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
