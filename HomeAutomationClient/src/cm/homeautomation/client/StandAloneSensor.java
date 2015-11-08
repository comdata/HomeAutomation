package cm.homeautomation.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import cm.homeautomation.entities.SensorData;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.base.HumiditySensor;
import cm.homeautomation.sensors.base.TechnicalSensor;
import cm.homeautomation.sensors.base.TemperatureSensor;

public class StandAloneSensor extends Thread {
	Map<String, TechnicalSensor> sensorList = new HashMap<String, TechnicalSensor>();
	private String roomId;
	private String url;

	public StandAloneSensor(String roomId, String url) {
		this.roomId = roomId;
		this.url = url;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		Properties props = new Properties();
		try {
			props.load(this.getClass().getClassLoader().getResourceAsStream("sensor.properties"));
		} catch (IOException e) {
			System.out.println("Could not find sensors properties!");
			e.printStackTrace();
			return;
		}

		for (int i = 0; true; i++) {
			String typeStr = props.getProperty(String.format("sensor%d.type", i));
			if (typeStr == null) {
				break;
			}

			String technicalType = props.getProperty(String.format("sensor%d.technicalType", i));
			if (technicalType == null) {
				break;
			}

			String pin = props.getProperty(String.format("sensor%d.pin", i));
			if (pin == null) {
				break;
			}

			String id = props.getProperty(String.format("sensor%d.id", i));
			if (id == null) {
				break;
			}

			switch (typeStr) {
			case "TEMPERATURE":
				sensorList.put(id, new TemperatureSensor(technicalType, pin));
				break;
			case "HUMIDITY":
				sensorList.put(id, new HumiditySensor(technicalType, pin));
				break;
			case "PRESSURE":
				sensorList.put(id, new PressureSensor(technicalType, pin));
				break;
			}
		}

		Client c = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
		WebTarget r = c.target(url);

		while (true) {

			try {

				Set<String> sensorKeys = sensorList.keySet();

				for (String sensor : sensorKeys) {
					SensorDataSaveRequest saveRequest = new SensorDataSaveRequest();
					TechnicalSensor technicalSensor = sensorList.get(sensor);
					SensorData sensorData = new SensorData();

					String value = "";

					// repeat sensor aquisition
					for (int i = 0; i < 10; i++) {
						value = technicalSensor.getValue();
						System.out.println("Read: " + value);
						if (Float.parseFloat(value) > 2) {
							break;
						} else {
							Thread.sleep(500);
						}
					}

					if (Float.parseFloat(value) < 0) {
						continue;
					}

					sensorData.setValue(value);

					saveRequest.setSensorData(sensorData);
					saveRequest.setSensorId(Long.parseLong(sensor));

					System.out.println(sensorData.getValue());
					try {
						Response response = r.request(MediaType.APPLICATION_JSON)
								.post(Entity.entity(saveRequest, MediaType.APPLICATION_JSON));
						System.out.println("Status: " + response.getStatus());
					} catch (Exception e) {

					}

				}

				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		StandAloneActor standAloneActor = new StandAloneActor();
		standAloneActor.start();
		StandAloneSensor standAloneSensor = new StandAloneSensor(args[0], args[1]);
		standAloneSensor.start();

		if (args.length > 2) {

			StandAloneRFSniffer standAloneRFSniffer = new StandAloneRFSniffer(args[2]);
			standAloneRFSniffer.start();
		}

	}
}
