package cm.homeautomation.dashbutton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.LogManager;
import org.dhcp4java.DHCPPacket;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButtonRange;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.AutoCreateInstance;

//import com.github.shynixn.dashbutton.DashButtonListener;
@AutoCreateInstance
public class DashButtonService {

	public static void main(String[] args) {

		new DashButtonService();

	}

	public DashButtonService() {
		System.out.println("Creating Dashbutton Service");
		this.run();
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

	private boolean isDashButton(String mac) {
		if (mac == null) {
			throw new IllegalArgumentException("MAC is NULL");
		}
		final String vendorCode = mac.substring(0, 6);

		final EntityManager em = EntityManagerService.getNewManager();

		try {
			final DashButtonRange singleResult = (DashButtonRange) em
					.createQuery("select dbr from DashButtonRange dbr where dbr.range=:vendor")
					.setParameter("vendor", vendorCode).getSingleResult();

			if (singleResult != null) {
				return true;
			}
		} catch (final NoResultException e) {

		}
		LogManager.getLogger(this.getClass()).trace("vendorCode: " + vendorCode);

		return false;
	}

	@AutoCreateInstance
	public void run() {
		System.out.println("Creating runner");
		final Runnable dashbuttonRunner = new Runnable() {
			HashMap<String, Date> timeFilter = new HashMap<>();

			@Override
			public void run() {

				final int listenPort = 67;
				final int MAX_BUFFER_SIZE = 1000;

				try (DatagramSocket socket = new DatagramSocket(listenPort);) {
					System.out.println("Start listening");

					final byte[] payload = new byte[MAX_BUFFER_SIZE];

					final DatagramPacket p = new DatagramPacket(payload, payload.length);
					// System.out.println("Success! Now listening on port " + listenPort + "...");

					// server is always listening
					final boolean listening = true;
					while (listening) {
						try {
							System.out.println("Listening on port " + listenPort + "...");
							socket.receive(p); // throws i/o exception
							System.out.println("Received data");

							final DHCPPacket packet = DHCPPacket.getPacket(p);

							final String mac = packet.getHardwareAddress().getHardwareAddressHex();
							System.out.println("checking mac: " + mac);
							if (isDashButton(mac)) {
								System.out.println("found a dashbutton mac: " + mac);

								/*
								 * suppress events if they are to fast
								 *
								 */
								if (!timeFilter.containsKey(mac)) {
									timeFilter.put(mac, new Date(1));
								}

								final Date filterTime = timeFilter.get(mac);

								if (((filterTime.getTime()) + 1000) < (new Date()).getTime()) {
									timeFilter.put(mac, new Date());
									EventBusService.getEventBus().post(new EventObject(new DashButtonEvent(mac)));
									System.out.println("send dashbutton event");
								}

							} else {
								System.out.println("not a dashbutton: " + mac);
							}
						} catch (final SocketException e) {
							LogManager.getLogger(this.getClass()).error("socket exeception", e);
						} catch (final IOException e) {
							LogManager.getLogger(this.getClass()).error("IO exeception", e);
						}
					}
				} catch (final SocketException e) {
					LogManager.getLogger(this.getClass()).error("socket exeception", e);
				} catch (final IOException e) {
					LogManager.getLogger(this.getClass()).error("IO exeception", e);
				}
			}
		};

		System.out.println("Triggering start");
		new Thread(dashbuttonRunner).start();

		System.out.println("Start triggered");
	}

}
