package cm.homeautomation.jeromq.server;

import java.io.IOException;
import java.util.Random;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.sensors.Sensors;

public class JeroMQServerWorker implements Runnable {

	private ZContext ctx;
	private static Random rand = new Random(System.nanoTime());

	public JeroMQServerWorker(ZContext ctx) {
		this.ctx = ctx;
	}

	public void run() {
		Socket worker = ctx.createSocket(ZMQ.DEALER);
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

			ObjectMapper mapper = new ObjectMapper();


			try {
				SensorDataSaveRequest sensorData = mapper.readValue(messageContent, SensorDataSaveRequest.class);

				sensorsService.saveSensorData(sensorData);
				address.send(worker, ZFrame.REUSE + ZFrame.MORE);
				content.send(worker, ZFrame.REUSE);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			address.destroy();
			content.destroy();
		}
		ctx.destroy();
	}
}
