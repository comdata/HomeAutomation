package cm.homeautomation.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.zeromq.ZMQ;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.entities.SensorData;
import cm.homeautomation.planes.PlaneSensor;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.base.HumiditySensor;
import cm.homeautomation.sensors.base.TechnicalSensor;
import cm.homeautomation.sensors.base.TemperatureSensor;
import cm.homeautomation.tv.PanasonicTVSensor;

public class StandAloneSensor extends Thread {
	Map<String, TechnicalSensor> sensorList = new HashMap<String, TechnicalSensor>();
	private String roomId;
	private String url;
	private int timeout=60;

	public StandAloneSensor(String roomId, String url) {
		this.roomId = roomId;
		this.url = url;
	}

	@Override
	public void run() {
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
			case "PLANE":
				sensorList.put(id, new PlaneSensor(technicalType, pin));
				break;
			case "PANASONIC-TV":
				sensorList.put(id, new PanasonicTVSensor(technicalType,pin));
				break;
			}
		}

		Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
		client.property(ClientProperties.CONNECT_TIMEOUT, 60000);
		client.property(ClientProperties.READ_TIMEOUT, 60000);

		WebTarget r = client.target(url);

		while (true) {

			try {

				Set<String> sensorKeys = sensorList.keySet();

				for (String sensor : sensorKeys) {
					SensorDataSaveRequest saveRequest = new SensorDataSaveRequest();
					TechnicalSensor technicalSensor = sensorList.get(sensor);
					SensorData sensorData = new SensorData();

					String value = "";

					// TODO fix
					// repeat sensor aquisition
					for (int i = 0; i < 10; i++) {
						value = technicalSensor.getValue();
						System.out.println("Read: " + value);
						if (Float.parseFloat(value) > 2 || ("PLANE".equals(technicalSensor.getType())) || ("PANASONIC-TV".equals(technicalSensor.getType())) ) {
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
					
					
					//	RESTDeliveryMethod(r, saveRequest);
					
					callJeroMQDeliveryMethod(saveRequest);
					
				}

				Thread.sleep(this.timeout * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void callJeroMQDeliveryMethod(SensorDataSaveRequest saveRequest) {
		


		//Object to JSON in String
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			String jsonInString = mapper.writeValueAsString(saveRequest);
			
			ZMQ.Context context = ZMQ.context(1);
	        ZMQ.Socket socket = context.socket(ZMQ.REQ);
	        socket.connect("tcp://192.168.1.57:5570");

	        socket.send(jsonInString.getBytes(), 0);
	        //String result = new String(socket.recv(0));

	        socket.close();
	        context.term();
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void RESTDeliveryMethod(WebTarget r, SensorDataSaveRequest saveRequest) {
		try {
			
			r.request(MediaType.APPLICATION_JSON).async()
			.post( Entity.entity(saveRequest, MediaType.APPLICATION_JSON), new InvocationCallback<Response>() {

				@Override
				public void completed(Response response) {
					// TODO Auto-generated method stub

					System.out.println("Status: " + response.getStatus());
				}

				@Override
				public void failed(Throwable throwable) {
					// TODO Auto-generated method stub
					System.out.println("Error message: " + throwable.getMessage());
				}});
			
		} catch (Exception e) {

		}
	}

	public static void main(String[] args) {
		StandAloneActor standAloneActor = new StandAloneActor();
		standAloneActor.start();
		StandAloneSensor standAloneSensor = new StandAloneSensor(args[0], args[1]);
		
		if (args.length>3) {
			standAloneSensor.setTimeout(args[3]);
		}
		
		standAloneSensor.start();

		if (args.length > 2) {

			StandAloneRFSniffer standAloneRFSniffer = new StandAloneRFSniffer(args[2]);
			standAloneRFSniffer.start();
		}

	}

	private void setTimeout(String timeout) {
		this.timeout=Integer.parseInt(timeout);
		
	}
}
