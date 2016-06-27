package cm.homeautomation.sensors;

public class SensorDataRoomPayload {
	private float temperature;
	private float humidity;
	private float pressure;
	private float vcc;
	private float lux;
	private float qnh;
	private float qfe;
	private float altitude;

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

	public float getLux() {
		return lux;
	}

	public void setLux(float lux) {
		this.lux = lux;
	}

	public float getQnh() {
		return qnh;
	}

	public void setQnh(float qnh) {
		this.qnh = qnh;
	}

	public float getQfe() {
		return qfe;
	}

	public void setQfe(float qfe) {
		this.qfe = qfe;
	}

	public float getAltitude() {
		return altitude;
	}

	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
}
