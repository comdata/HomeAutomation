package cm.homeautomation.sensors.base;

import java.util.ArrayList;
import java.util.List;

public class Sensors {

	private List<TechnicalSensor> sensors;
	private static Sensors instance;
	
	public Sensors() {
		
		
		/*TechnicalSensor tempSensor=new TemperatureSensor();
		this.getSensors().add(tempSensor);
		TechnicalSensor humiditySensor=new HumiditySensor();
		this.getSensors().add(humiditySensor);*/
	}
	
	public static Sensors getInstance() {
		if (instance==null) {
			instance=new Sensors();
		}
		
		return instance;
	}
	
	public List<TechnicalSensor> getSensors() {
		if(sensors==null) {
			sensors=new ArrayList<TechnicalSensor>();
		}
		return sensors;
	}

	public void setSensors(List<TechnicalSensor> sensors) {
		this.sensors = sensors;
	}
}
