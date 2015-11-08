package cm.homeautomation.client;

import cm.homeautomation.bmp180.BMP180;

public class BMP180SensorTest {

	public static void main(String[] args) {
		BMP180 bmp180 = new BMP180();
		
		try {
			System.out.println("Temp: "+Float.toString(bmp180.readTemperature()));
			System.out.println("Pressure: "+Float.toString(bmp180.readPressure()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
