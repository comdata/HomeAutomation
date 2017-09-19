package cm.homeautomation.dashbutton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.persistence.EntityManager;

import org.dhcp4java.DHCPPacket;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButtonRange;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.AutoCreateInstance;

//import com.github.shynixn.dashbutton.DashButtonListener;
@AutoCreateInstance
public class DashButtonService {

	public DashButtonService() {
		System.out.println("Creating Dashbutton Service");
		this.run();
	}

	@AutoCreateInstance
	public void run() {
		System.out.println("Creating runner");
		Runnable dashbuttonRunner = new Runnable() {
			public void run() {

				int listenPort = 67;
				int MAX_BUFFER_SIZE = 1500;

				DatagramSocket socket = null;
				try {
					System.out.println("Start listening");
					socket = new DatagramSocket(listenPort); // ipaddress? throws socket exception

					byte[] payload = new byte[MAX_BUFFER_SIZE];
					int length = 1500;
					DatagramPacket p = new DatagramPacket(payload, length);
					// System.out.println("Success! Now listening on port " + listenPort + "...");
					System.out.println("Listening on port " + listenPort + "...");

					// server is always listening
					boolean listening = true;
					while (listening) {
						try {
							socket.receive(p); // throws i/o exception
							System.out.println("Received data");

							DHCPPacket packet = DHCPPacket.getPacket(p);

							String mac = packet.getHardwareAddress().getHardwareAddressHex();
							System.out.println("checking mac: " + mac);
							if (isDashButton(mac)) {
								System.out.println("found a dashbutton mac: " + mac);

								EventBusService.getEventBus().post(new EventObject(new DashButtonEvent(mac)));

							} else {
								System.out.println("not a dashbutton: " + mac);
							}
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		System.out.println("Triggering start");
		new Thread(dashbuttonRunner).start();

		System.out.println("Start triggered");
	}

	/*
	 * public DashButtonService() { String dashButtonIp = "192.168.1.90"; //Your
	 * static dashButton ip DashButtonListener listener =
	 * DashButtonListener.fromIpAddress(dashButtonIp); listener.register(new
	 * Runnable() {
	 * 
	 * @Override public void run() { //Gets called when the dashButton with the
	 * given ip in the local network is pressed
	 * System.out.println("Button pressed"); } });
	 * 
	 * 
	 * }
	 */

	public static void main(String[] args) {

		new DashButtonService();

	}

	private boolean isDashButton(String mac) {
		if (mac == null) {
			throw new IllegalArgumentException("MAC is NULL");
		}
		String vendorCode = mac.substring(0, 6);

		EntityManager em = EntityManagerService.getNewManager();

		DashButtonRange singleResult = (DashButtonRange) em
				.createQuery("select db from DashButtonRange dbr where range=:vendor")
				.setParameter("vendor", vendorCode).getSingleResult();

		if (singleResult != null) {
			return true;
		}

		System.out.println(vendorCode);

		return false;
	}

}
