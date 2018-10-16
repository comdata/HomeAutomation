package cm.homeautomation.sensors.base;

import java.io.IOException;

import se.hirt.w1.impl.DHTHygrometer;
import se.hirt.w1.impl.DHTSensor;
import se.hirt.w1.impl.DHTType;
import org.apache.logging.log4j.LogManager;


public class HumiditySensor implements TechnicalSensor {

	private DHTHygrometer dhtTemperature;

	public HumiditySensor(String type, String pin) {
		DHTSensor sensor = new DHTSensor(DHTType.getType(type), Integer.parseInt(pin));
		dhtTemperature = new DHTHygrometer(sensor);
	}
	
	@Override
	public String getValue() {
		Number value;
		try {
			value = dhtTemperature.getValue();
			return value.toString();
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).error("IO Exception",e );
		}
		return null;
		
	}

	@Override
	public String getType() {

		return null;
	}

}
