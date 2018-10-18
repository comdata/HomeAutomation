package cm.homeautomation.sensors.base;

import java.io.IOException;

import se.hirt.w1.impl.DHTSensor;
import se.hirt.w1.impl.DHTTemperature;
import se.hirt.w1.impl.DHTType;

public class TemperatureSensor implements TechnicalSensor {

	private DHTTemperature dhtTemperature;

	public TemperatureSensor(String type, String pin) {

		DHTSensor sensor = new DHTSensor(DHTType.getType(type), Integer.parseInt(pin));
		dhtTemperature = new DHTTemperature(sensor);
	}

	@Override
	public String getValue() {
		Number value;
		try {
			value = dhtTemperature.getValue();
			return value.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
