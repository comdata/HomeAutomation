package cm.homeautomation.client;

import cm.homeautomation.bmp180.BMP180;
import cm.homeautomation.sensors.base.TechnicalSensor;

public class PressureSensor implements TechnicalSensor {

	
	
	private BMP180 bmp180;

	public PressureSensor(String technicalType, String pin) {
		bmp180 = new BMP180();
	}

	@Override
	public String getValue() {
		try {
			return Float.toString(bmp180.readPressure()/100).replace(",",".");
		} catch (Exception e) {
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
