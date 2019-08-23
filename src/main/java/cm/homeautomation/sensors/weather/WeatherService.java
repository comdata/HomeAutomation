package cm.homeautomation.sensors.weather;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;

import com.amphibian.weather.request.Feature;
import com.amphibian.weather.request.WeatherRequest;
import com.amphibian.weather.response.WeatherResponse;

import cm.homeautomation.services.overview.WeatherData;

/**
 * read wether data from the service
 * 
 * @author mertins
 *
 */
public class WeatherService {

	public static WeatherData getWeather() {
		String apiKey = "";
		String city = "";
		String country = "";
		String zmw = "";
		Properties props = new Properties();
		try {
			File file = new File("weather.properties");
			LogManager.getLogger(WeatherService.class).info("Weather properties: {}", file.getAbsolutePath());
			FileReader fileReader = new FileReader(file);

			props.load(fileReader);
			apiKey = props.getProperty("apiKey");
			city = props.getProperty("city");
			country = props.getProperty("country");
			zmw = props.getProperty("zmw");
		} catch (IOException e) {
			LogManager.getLogger(WeatherService.class).info("Could not find weather properties!");
			return null;
		}

		WeatherData weatherData = new WeatherData();

		WeatherRequest req = new WeatherRequest();
		req.setZmw(zmw);
		req.setApiKey(apiKey);
		req.addFeature(Feature.CONDITIONS);
		req.addFeature(Feature.FORECAST);
		WeatherResponse weather = req.query(country, city);
		LogManager.getLogger(WeatherData.class).debug(weather.getResponse());

		if (weather.getCurrent_observation() != null) {
			weatherData.setHumidity(weather.getCurrent_observation().getRelativeHumidity().replace("%", ""));
			weatherData.setTempC(weather.getCurrent_observation().getTempC());
			weatherData.setPressure(Float.toString(weather.getCurrent_observation().getPressureMb()));
			LogManager.getLogger(WeatherService.class).debug(weatherData);
		} else {
			LogManager.getLogger(WeatherService.class).info("Could not access weather information");
		}

		return weatherData;
	}

	public static void main(String[] args) {
		WeatherData weather = WeatherService.getWeather();
		if (weather != null) {
			LogManager.getLogger(WeatherService.class).info(weather.getTempC());
			LogManager.getLogger(WeatherService.class).info(weather.getHumidity());
		}
	}
}
