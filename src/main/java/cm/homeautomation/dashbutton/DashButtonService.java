package cm.homeautomation.dashbutton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.dhcp4java.DHCPPacket;

import cm.homeautomation.entities.DashButtonRange;
import cm.homeautomation.eventbus.EventObject;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.EventBus;

@Startup
@ApplicationScoped
public class DashButtonService {

	@Inject
	EventBus bus;
	
	@Inject
	EntityManager em;

	HashMap<String, Date> timeFilter = new HashMap<>();

	void startup(@Observes StartupEvent event) {

		Runnable runner = () -> {
			final int listenPort = 67;
			final int MAX_BUFFER_SIZE = 1000;

			try (DatagramSocket socket = new DatagramSocket(listenPort);) {
//				LogManager.getLogger(this.getClass()).debug("start listening");
				System.out.println("Start listening.");
				final byte[] payload = new byte[MAX_BUFFER_SIZE];

				final DatagramPacket p = new DatagramPacket(payload, payload.length);

				// server is always listening
				final boolean listening = true;
				while (listening) {
					System.out.println("listening");
					listenAndReceive(listenPort, socket, p);
				}
			} catch (final SocketException e) {
				e.printStackTrace();
				// LogManager.getLogger(this.getClass()).error("socket exeception", e);
			}
		};
		new Thread(runner).start();
	}

	private void listenAndReceive(final int listenPort, DatagramSocket socket, final DatagramPacket p) {
		try {
//				LogManager.getLogger(this.getClass()).debug("Listening on port " + listenPort + "...");

			socket.receive(p); // throws i/o exception
//				LogManager.getLogger(this.getClass()).debug("Received data");

			final DHCPPacket packet = DHCPPacket.getPacket(p);

			Runnable runner = () -> {

				final String mac = packet.getHardwareAddress().getHardwareAddressHex();
//				LogManager.getLogger(this.getClass()).debug("checking mac: " + mac);
				if (isDashButton(mac)) {
//					LogManager.getLogger(this.getClass()).debug("found a dashbutton mac: " + mac);

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
						bus.publish("EventObject", new EventObject(new DashButtonEvent(mac)));
//						LogManager.getLogger(this.getClass()).debug("send dashbutton event");
					}

				} else {
//					LogManager.getLogger(this.getClass()).debug("not a dashbutton: " + mac);
				}
			};
			new Thread(runner).start();
		} catch (final SocketException e) {
//				LogManager.getLogger(this.getClass()).error("socket exeception", e);
		} catch (final IOException e) {
//				LogManager.getLogger(this.getClass()).error("IO exeception", e);
		}
	}

	private boolean isDashButton(String mac) {
		if (mac == null) {
			throw new IllegalArgumentException("MAC is NULL");
		}
		final String vendorCode = mac.substring(0, 6);

		try {
			final DashButtonRange singleResult = em
					.createQuery("select dbr from DashButtonRange dbr where dbr.range=:vendor", DashButtonRange.class)
					.setParameter("vendor", vendorCode).getSingleResult();

			if (singleResult != null) {
				return true;
			}
		} catch (final NoResultException e) {
//				LogManager.getLogger(this.getClass()).error(e);
		}
//			LogManager.getLogger(this.getClass()).trace("vendorCode: " + vendorCode);

		return false;
	}

	public static void main(String[] args) {

		new DashButtonService();

	}

}
