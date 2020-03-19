package cm.homeautomation.sensors;

import java.util.ArrayList;
import java.util.List;

public class SensorValues {
	private String sensorName;
	private List<SensorValue> values;
	
	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public List<SensorValue> getValues() {
		if (values==null) {
			values=new ArrayList<>();
		}
		return values;
	}

	public void setValues(List<SensorValue> values) {
		this.values = values;
	}
	
}
