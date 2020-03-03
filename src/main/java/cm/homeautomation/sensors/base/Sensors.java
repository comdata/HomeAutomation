package cm.homeautomation.sensors.base;

import java.util.ArrayList;
import java.util.List;

public class Sensors {

	private List<TechnicalSensor> sensorList;
	private static Sensors instance;
	
	public Sensors() {
		// do nothing
	}
	
	public static Sensors getInstance() {
		if (instance==null) {
			instance=new Sensors();
		}
		
		return instance;
	}
	
	public List<TechnicalSensor> getSensors() {
		if(sensorList==null) {
			sensorList=new ArrayList<>();
		}
		return sensorList;
	}

	public void setSensors(List<TechnicalSensor> sensors) {
		this.sensorList = sensors;
	}
}
