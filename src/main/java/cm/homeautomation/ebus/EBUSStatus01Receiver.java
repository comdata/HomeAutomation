package cm.homeautomation.ebus;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.sensors.Sensors;

public class EBUSStatus01Receiver {

	public EBUSStatus01Receiver() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void receive(EventObject eventObject) {
		if (eventObject.getData() instanceof EBusMessageEvent) {
			EBusMessageEvent messageEvent = (EBusMessageEvent) eventObject.getData();

			if ("ebusd/bai/Status01".equals(messageEvent.getTopic())) {
				String outsideTemp = messageEvent.getMessageString().split(";")[2];
				SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
				SensorData sensorData = new SensorData();
				sensorData.setValue(outsideTemp);
				Sensor sensor = new Sensor();
				sensor.setSensorName("OUTSIDETEMP");
				sensor.setSensorType("OUTSIDETEMP");
				sensorData.setSensor(sensor);
				sensorDataSaveRequest.setSensorData(sensorData);
				Sensors sensorsInstance = Sensors.getInstance();
				if (sensorsInstance != null) {
					sensorsInstance.saveSensorData(sensorDataSaveRequest);
				} else {
					LogManager.getLogger(EBUSStatus01Receiver.class).error("Sensors class not initialized correctly. Got no instance back.");

				}

			}
		} else {
			LogManager.getLogger(EBUSStatus01Receiver.class).error("Empty event received.");
		}
	}
}
