package cm.homeautomation.services.base;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.base.HumiditySensor;
import cm.homeautomation.sensors.base.TechnicalSensor;
import cm.homeautomation.sensors.base.TemperatureSensor;
import cm.homeautomation.services.sensors.Sensors;

public class SensorDataThread extends Thread {

	HashMap<Sensor, TechnicalSensor> sensorList = new HashMap<Sensor, TechnicalSensor>();
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
		run=true;
		Sensors sensorService = new Sensors();
		
		while (run) {
			try {

				List sensors = em.createQuery("select s FROM Sensor s where s.sensorPosition='LOCAL'").getResultList();

				for (Object sensorObject : sensors) {
					if (sensorObject instanceof Sensor) {
						Sensor sensor = (Sensor) sensorObject;

						if (!sensorList.containsKey(sensor)) {

							switch (sensor.getSensorType()) {
							case "TEMPERATURE":
								sensorList.put(sensor,
										new TemperatureSensor(sensor.getSensorTechnicalType(), sensor.getSensorPin()));
								break;
							case "HUMIDITY":
								sensorList.put(sensor,
										new HumiditySensor(sensor.getSensorTechnicalType(), sensor.getSensorPin()));
								break;
							}
						}

					}

				}

				Set<Sensor> sensorKeys = sensorList.keySet();

				for (Sensor sensor : sensorKeys) {
					TechnicalSensor technicalSensor = sensorList.get(sensor);
					
					SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
					
					
					SensorData sensorData = new SensorData();
					
					sensorData.setValue(technicalSensor.getValue());
				
					sensorDataSaveRequest.setSensorData(sensorData);
					sensorDataSaveRequest.setSensorId(sensor.getId());
					
					sensorService.saveSensorData(sensorDataSaveRequest);
				}

				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

	}
}
