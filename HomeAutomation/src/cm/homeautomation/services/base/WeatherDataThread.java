package cm.homeautomation.services.base;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Weather;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.weather.WeatherService;
import cm.homeautomation.services.overview.WeatherData;
import cm.homeautomation.services.sensors.Sensors;

public class WeatherDataThread {

	private static WeatherDataThread instance;

	private Sensors sensorService;

	public WeatherDataThread() {
		init();

	}

	private void init() {
		sensorService = new Sensors();
	}

	private Sensor getSensorType(String sensorType) {
		EntityManager em = EntityManagerService.getNewManager();
		Object sensorResultObj = em
				.createQuery(
						"select s from Sensor s where s.sensorType=:sensorType and s.room=(select r from Room r where r.roomName='Draussen')")
				.setParameter("sensorType", sensorType).getSingleResult();

		if (sensorResultObj instanceof Sensor) {
			return (Sensor) sensorResultObj;
		}
		em.close();
		return null;
	}

	public static WeatherDataThread getInstance() {
		if (instance == null) {
			instance = new WeatherDataThread();
		}

		return instance;
	}

	public static void loadWeather(String[] params) {
		WeatherDataThread.getInstance().loadAndStoreWeather();
	}



	public void loadAndStoreWeather() {
		init();
		EntityManager em = EntityManagerService.getNewManager();
		try {
			WeatherData weatherData = WeatherService.getWeather();
			em.getTransaction().begin();
			List results = em.createQuery("select w from Weather w").getResultList();

			Weather weather = null;

			if (!results.isEmpty()) {
				Object singleResult = results.get(0);
				if (singleResult instanceof Weather) {
					weather = (Weather) singleResult;
				}
			} else {
				weather = new Weather();
			}

			weather.setFetchDate(new Date());
			weather.setTempC(Float.toString(weatherData.getTempC()));
			weather.setHumidity(weatherData.getHumidity());
			weather.setPressure(weatherData.getPressure());
			em.persist(weather);
			em.getTransaction().commit();

			Sensor weatherHumiditySensor = getSensorType("HUMIDITY");
			Sensor weatherTemperatureSensor = getSensorType("TEMPERATURE");
			Sensor weatherPressureSensor = getSensorType("PRESSURE");

			if (weatherTemperatureSensor != null) {
				SensorData weatherSensorTemperatureData = new SensorData();
				weatherSensorTemperatureData.setValue(Float.toString(weatherData.getTempC()));
				SensorDataSaveRequest weatherTemperatureSaveRequest = new SensorDataSaveRequest();
				weatherTemperatureSaveRequest.setSensorData(weatherSensorTemperatureData);
				weatherTemperatureSaveRequest.setSensorId(weatherTemperatureSensor.getId());
				sensorService.saveSensorData(weatherTemperatureSaveRequest);
			}

			if (weatherHumiditySensor != null) {
				SensorData weatherSensorHumidityData = new SensorData();
				weatherSensorHumidityData.setValue(weatherData.getHumidity());
				SensorDataSaveRequest weatherHumiditySaveRequest = new SensorDataSaveRequest();
				weatherHumiditySaveRequest.setSensorData(weatherSensorHumidityData);
				weatherHumiditySaveRequest.setSensorId(weatherHumiditySensor.getId());
				sensorService.saveSensorData(weatherHumiditySaveRequest);
			}
			
			if (weatherPressureSensor != null) {
				SensorData weatherPressureData = new SensorData();
				weatherPressureData.setValue(weatherData.getPressure());
				SensorDataSaveRequest weatherHumiditySaveRequest = new SensorDataSaveRequest();
				weatherHumiditySaveRequest.setSensorData(weatherPressureData);
				weatherHumiditySaveRequest.setSensorId(weatherPressureSensor.getId());
				sensorService.saveSensorData(weatherHumiditySaveRequest);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		em.close();
	}
}
