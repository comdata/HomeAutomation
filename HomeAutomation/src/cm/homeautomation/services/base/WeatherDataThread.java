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
	private EntityManager em;
	private Sensor weatherTemperatureSensor;
	private Sensors sensorService;
	private Sensor weatherHumiditySensor;
	
	public WeatherDataThread() {
		em = EntityManagerService.getNewManager();
		
		Object weatherResultObj = em.createQuery("select s from Sensor s where s.sensorType=:sensorType and s.room=(select r from Room r where r.roomName='Draussen')")
				.setParameter("sensorType", "TEMPERATURE").getSingleResult();
		weatherTemperatureSensor = null;
		sensorService = new Sensors();

		if (weatherResultObj instanceof Sensor) {
			weatherTemperatureSensor = (Sensor) weatherResultObj;
		}
		
		Object weatherHumidityResultObj = em.createQuery("select s from Sensor s where s.sensorType=:sensorType and s.room=(select r from Room r where r.roomName='Draussen')")
				.setParameter("sensorType", "HUMIDITY").getSingleResult();
		weatherTemperatureSensor = null;
		sensorService = new Sensors();

		if (weatherHumidityResultObj instanceof Sensor) {
			weatherHumiditySensor = (Sensor) weatherResultObj;
		}
		
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
				SensorData weatherSensorData = new SensorData();
				weatherSensorData.setValue(Float.toString(weatherData.getTempC()));
				SensorDataSaveRequest weatherSaveRequest = new SensorDataSaveRequest();
				weatherSaveRequest.setSensorData(weatherSensorData);
				weatherSaveRequest.setSensorId(weatherTemperatureSensor.getId());
				sensorService.saveSensorData(weatherSaveRequest);
			}
			
			if (weatherHumiditySensor != null) {
				SensorData weatherSensorData = new SensorData();
				weatherSensorData.setValue(weatherData.getHumidity());
				SensorDataSaveRequest weatherSaveRequest = new SensorDataSaveRequest();
				weatherSaveRequest.setSensorData(weatherSensorData);
				weatherSaveRequest.setSensorId(weatherHumiditySensor.getId());
				sensorService.saveSensorData(weatherSaveRequest);
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		em.close();
	}
}
