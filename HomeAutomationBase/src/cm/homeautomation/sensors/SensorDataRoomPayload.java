package cm.homeautomation.services.sensors;

public class SensorDataRoomPayload {
	private float temperature;
	private float humidity;
	private float pressure;
	private float vcc;

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getHumidity() {
		return humidity;
	}

	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	public float getPressure() {
		return pressure;
	}

	public void setPressure(float pressure) {
		this.pressure = pressure;
	}

	public float getVcc() {
		return vcc;
	}

	public void setVcc(float vcc) {
		this.vcc = vcc;
	}
}
