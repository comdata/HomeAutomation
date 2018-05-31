package cm.homeautomation.services.networkmonitor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.dashbutton.DashButtonEvent;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.networkmonitor.NetworkScanner;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * service to get all hosts from the {@link NetworkScanner} internal list
 *
 * @author christoph
 *
 */
@Path("networkdevices")
public class NetworkDevicesService extends BaseService {

	private static NetworkDevicesService instance;

	public static NetworkDevicesService getInstance() {
		return instance;
	}

	public static void setInstance(final NetworkDevicesService instance) {
		NetworkDevicesService.instance = instance;
	}

	private EntityManager em;

	private final int PORT = 9;

	private final String broadcastIpAddress = "192.168.1.255";

	public NetworkDevicesService() {
		EventBusService.getEventBus().register(this);
		setInstance(this);
	}

	@GET
	@Path("delete/{name}/{ip}/{mac}")
	public GenericStatus delete(@PathParam("name") final String name, @PathParam("ip") final String ip,
			@PathParam("mac") final String macStsr) {
		// TODO perform delete
		return new GenericStatus(true);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	private byte[] getMacBytes(final String macStr) throws IllegalArgumentException {
		final byte[] bytes = new byte[6];
		final String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Invalid hex digit in MAC address.");
		}
		return bytes;
	}

	/**
	 * wake up based on a dashbutton event
	 *
	 * @param event
	 */
	@Subscribe
	public void handleEvent(final EventObject event) {

		final Object data = event.getData();

		if (data instanceof DashButtonEvent) {

			em = EntityManagerService.getNewManager();

			final DashButtonEvent dbEvent = (DashButtonEvent) data;

			final String mac = dbEvent.getMac();

			final List<DashButton> resultList = em.createQuery("select db from DashButton db where db.mac=:mac")
					.setParameter("mac", mac).getResultList();

			DashButton dashButton = null;
			if ((resultList != null) && !resultList.isEmpty()) {
				for (final DashButton db : resultList) {
					dashButton = db;
					break;
				}
			}

			if (dashButton != null) {
				final NetworkDevice referencedNetworkDevice = dashButton.getReferencedNetworkDevice();

				if (referencedNetworkDevice != null) {

					final String networkDeviceMac = referencedNetworkDevice.getMac();

					this.wakeUp(networkDeviceMac);
				}
			}
		}
	}

	@Path("getAll")
	@GET
	public List<NetworkDevice> readAll() {
		final EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<NetworkDevice> resultList = em.createQuery("select n from NetworkDevice n").getResultList();

		if (resultList == null) {
			resultList = new ArrayList<>();
		}
		em.close();
		return resultList;
	}

	@GET
	@Path("wake/{mac}")
	public GenericStatus wakeUp(@PathParam("mac") final String macStr) {
		try {
			final byte[] macBytes = getMacBytes(macStr);
			final byte[] bytes = new byte[6 + (16 * macBytes.length)];
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) 0xff;
			}
			for (int i = 6; i < bytes.length; i += macBytes.length) {
				System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
			}

			final InetAddress address = InetAddress.getByName(broadcastIpAddress);
			final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
			final DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			socket.close();

			LogManager.getLogger(this.getClass()).info("Wake-on-LAN packet sent.");
			return new GenericStatus(true);
		} catch (final Exception e) {
			LogManager.getLogger(this.getClass()).info("Failed to send Wake-on-LAN packet: + e");
			return new GenericStatus(false);
		}

	}

}
