package cm.homeautomation.jeromq.server;

import java.io.IOException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.sensors.JSONSensorDataBase;
import cm.homeautomation.sensors.SensorDataRoomSaveRequest;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.sensors.Sensors;

public class JeroMQServerWorker implements Runnable {

	private ZContext ctx;
	private static Random rand = new Random(System.nanoTime());

	public JeroMQServerWorker(ZContext ctx) {
		this.ctx = ctx;
	}

	public void run() {
		try (Socket worker = ctx.createSocket(ZMQ.DEALER)) {
			worker.connect("inproc://backend");

			Sensors sensorsService = new Sensors();

			while (!Thread.currentThread().isInterrupted()) {
				// The DEALER socket gives us the address envelope and message
				ZMsg msg = ZMsg.recvMsg(worker);
				ZFrame address = msg.pop();
				ZFrame header = msg.pop();
				ZFrame content = msg.pop();
				assert (content != null);
				msg.destroy();

				String messageContent = content.toString();
				LogManager.getLogger(this.getClass()).info("Got message: " + messageContent);

				ObjectMapper mapper = new ObjectMapper();

				try {

					messageContent = messageContent.replace("@class", "@c");

					messageContent = messageContent.replace("cm.homeautomation.sensors", "");

					messageContent = messageContent.replace("cm.homeautomation.transmission.TransmissionStatusData",
							".TransmissionStatusData");

					LogManager.getLogger(this.getClass()).info("message for deserialization: " + messageContent);

					JSONSensorDataBase sensorData = mapper.readValue(messageContent, JSONSensorDataBase.class);

					if (sensorData instanceof SensorDataSaveRequest) {
						sensorsService.saveSensorData((SensorDataSaveRequest) sensorData);
					} else if (sensorData instanceof SensorDataRoomSaveRequest) {
						sensorsService.save((SensorDataRoomSaveRequest) sensorData);
					}

					address.send(worker, ZFrame.REUSE + ZFrame.MORE);
					content.send(worker, ZFrame.REUSE);
				} catch (IOException e1) {
					LogManager.getLogger(this.getClass()).error(e1);
				}

				address.destroy();
				content.destroy();
			}
			ctx.destroy();
		}
	}
}
