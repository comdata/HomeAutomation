package cm.homeautomation.services.base;

import java.util.List;
import java.util.Date;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Weather;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.weather.WeatherService;
import cm.homeautomation.services.overview.WeatherData;
import cm.homeautomation.services.sensors.Sensors;
import jersey.repackaged.com.google.common.base.Stopwatch;

public class WeatherDataThread extends Thread {

	boolean run = true;

	public void stopThread() {
		run = false;
		this.interrupt();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		EntityManager em = EntityManagerService.getNewManager();

		run = true;

		Object weatherResultObj = em.createQuery("select s from Sensor s where s.sensorType=:sensorType")
				.setParameter("sensorType", "WEATHER").getSingleResult();
		Sensor weatherSensor = null;
		Sensors sensorService = new Sensors();

		if (weatherResultObj instanceof Sensor) {
			weatherSensor = (Sensor) weatherResultObj;
		}

		while (run) {
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

				if (weatherSensor != null) {
					SensorData weatherSensorData = new SensorData();
					weatherSensorData.setValue(Float.toString(weatherData.getTempC()));
					SensorDataSaveRequest weatherSaveRequest = new SensorDataSaveRequest();
					weatherSaveRequest.setSensorData(weatherSensorData);
					weatherSaveRequest.setSensorId(weatherSensor.getId());
					sensorService.saveSensorData(weatherSaveRequest);
				}

				try {
					Thread.sleep(30 * 60 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
