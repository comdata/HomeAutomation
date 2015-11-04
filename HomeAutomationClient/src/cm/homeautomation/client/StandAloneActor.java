package cm.homeautomation.client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.sensors.ActorMessage;

public class StandAloneActor extends Thread {

	boolean run = true;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		run = true;
		while (run) {
			System.out.println("Waiting for UDP Events");
			try {
				MulticastSocket socket = new MulticastSocket(5000);
				InetAddress group = InetAddress.getByName("239.1.1.1");
				socket.joinGroup(group);

				DatagramPacket packet;
				for (int i = 0; i < 5; i++) {
					byte[] buf = new byte[4096];
					packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);

					String received = new String(packet.getData());
					System.out.println("Got message: "+received );
					ActorMessage message=new ObjectMapper().readValue(received, ActorMessage.class);
					
					Runtime rt = Runtime.getRuntime();
					
					Process proc = rt.exec("send "+message.getHouseCode()+" "+message.getSwitchNo()+" "+message.getStatus());
					proc.waitFor();
				}

				socket.leaveGroup(group);
				socket.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void stopThread() {
		run = false;
		this.interrupt();
	}
}
