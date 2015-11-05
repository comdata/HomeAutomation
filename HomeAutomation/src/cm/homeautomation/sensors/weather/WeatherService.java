package cm.homeautomation.sensors.weather;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.amphibian.weather.request.Feature;
import com.amphibian.weather.request.WeatherRequest;
import com.amphibian.weather.response.WeatherResponse;

import cm.homeautomation.services.overview.WeatherData;

public class WeatherService {

	public static WeatherData getWeather() {
		String apiKey = "";
		String city = "";
		String country = "";
		Properties props = new Properties();
		try {
			props.load(new FileReader("weather.properties"));
			apiKey = props.getProperty("apiKey");
			city = props.getProperty("city");
			country = props.getProperty("country");
		} catch (IOException e) {
			System.out.println("Could not find sensors properties!");
			return null;
		}

		WeatherData weatherData = new WeatherData();

		WeatherRequest req = new WeatherRequest();
		req.setApiKey(apiKey);
		req.addFeature(Feature.CONDITIONS);
		req.addFeature(Feature.FORECAST);
		WeatherResponse weather = req.query(country, city);
		weatherData.setHumidity(weather.getCurrent_observation().getRelativeHumidity());
		weatherData.setTempC(weather.getCurrent_observation().getTempC());

		return weatherData;
	}

	public static void main(String[] args) {
		WeatherData weather = WeatherService.getWeather();
		System.out.println(weather.getTempC());
		System.out.println(weather.getHumidity());
	}
}
