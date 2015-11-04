package cm.homeautomation.sensors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SensorDatas {

	private List<SensorValues> sensorData;

	public List<SensorValues> getSensorData() {
		if (sensorData==null) {
			sensorData=new ArrayList<SensorValues>();
		}
		return sensorData;
	}

	public void setSensorData(List<SensorValues> sensorData) {
		this.sensorData = sensorData;
	} 
}
