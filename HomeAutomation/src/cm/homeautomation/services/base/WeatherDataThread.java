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

public class WeatherDataThread extends Thread {

	private static WeatherDataThread instance;
	boolean run = true;
	private Sensor weatherTemperatureSensor;
	private Sensors sensorService;
	private Sensor weatherHumiditySensor;
	
	public WeatherDataThread() {
		sensorService = new Sensors();

		EntityManager em = EntityManagerService.getNewManager();
		
		Object weatherResultObj = em.createQuery("select s from Sensor s where s.sensorType=:sensorType and s.room=(select r from Room r where r.roomName='Draussen')")
				.setParameter("sensorType", "TEMPERATURE").getSingleResult();
		weatherTemperatureSensor = null;

		if (weatherResultObj instanceof Sensor) {
			weatherTemperatureSensor = (Sensor) weatherResultObj;
		}
		
		Object weatherHumidityResultObj = em.createQuery("select s from Sensor s where s.sensorType=:sensorType and s.room=(select r from Room r where r.roomName='Draussen')")
				.setParameter("sensorType", "HUMIDITY").getSingleResult();
		weatherTemperatureSensor = null;

		if (weatherHumidityResultObj instanceof Sensor) {
			weatherHumiditySensor = (Sensor) weatherHumidityResultObj;
		}
		em.close();
		
	}
	
	public static WeatherDataThread getInstance() {
		if (instance==null) {
			instance=new WeatherDataThread();
		}
		
		return instance;
	}
	
	
	public static void loadWeather(String[] params) {
		WeatherDataThread.getInstance().loadAndStoreWeather();
	}
	

	public void stopThread() {
		run = false;
		this.interrupt();
	}

	@Override
	public void run() {
		super.run();
		

		run = true;

	

		while (run) {
			loadAndStoreWeather();
			try {
				Thread.sleep(30 * 60 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

	}

	public void loadAndStoreWeather() {
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
			em.persist(weather);
			em.getTransaction().commit();

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

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		em.close();
	}
}
