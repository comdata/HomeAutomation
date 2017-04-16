package cm.homeautomation.services.overview;

public class WeatherData {

	private float tempC;
	private String relativeHumidity;

	public void setTempC(float tempC) {
		this.tempC = tempC;		
	}
	
	public float getTempC() {
		return tempC;
	}

	public void setHumidity(String relativeHumidity) {
		this.relativeHumidity=relativeHumidity;
	}

	public String getHumidity() {
		return relativeHumidity;
	}
	
	@Override
	public String toString() {
		
		return "Temperature: "+tempC+" humidity: "+relativeHumidity;
	}

}
