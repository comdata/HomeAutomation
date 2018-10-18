package cm.homeautomation.hap;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.beowulfe.hap.HomekitAuthInfo;
import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.windowblind.WindowBlindService;
import cm.homeautomation.windowblind.WindowBlindsList;

@AutoCreateInstance
public class HAPService {
	private static final int PORT = 9123;

	private static HAPService instance;

	/**
	 * @return the instance
	 */
	public static HAPService getInstance() {
		if (instance == null) {
			instance = new HAPService();
		}

		return instance;
	}

	/**
	 * Replaces all german umlaute in the input string with the usual replacement
	 * scheme, also taking into account capitilization. A test String such as "Käse
	 * Köln Füße Öl Übel Äü Üß ÄÖÜ Ä Ö Ü ÜBUNG" will yield the result "Kaese Koeln
	 * Fuesse Oel Uebel Aeue Uess AEOEUe Ae Oe Ue UEBUNG"
	 *
	 * @param input
	 * @return the input string with replaces umlaute
	 */
	private static String replaceUmlaut(final String input) {

		// replace all lower Umlauts
		String o_strResult = input.replaceAll("ü", "ue").replaceAll("ö", "oe").replaceAll("ä", "ae").replaceAll("ß",
				"ss");

		// first replace all capital umlaute in a non-capitalized context (e.g.
		// Übung)
		o_strResult = o_strResult.replaceAll("Ü(?=[a-zäöüß ])", "Ue").replaceAll("Ö(?=[a-zäöüß ])", "Oe")
				.replaceAll("Ä(?=[a-zäöüß ])", "Ae");

		// now replace all the other capital umlaute
		o_strResult = o_strResult.replaceAll("Ü", "UE").replaceAll("Ö", "OE").replaceAll("Ä", "AE");

		return o_strResult;
	}

	/**
	 * @param instance
	 *            the instance to set
	 */
	public static void setInstance(final HAPService instance) {
		HAPService.instance = instance;
	}

	private EntityManager em;

	private HomekitRoot bridge;

	private Map<Long, HAPTemperatureSensor> temperatureSensors = new HashMap<>();

	private Map<Long, HAPHumiditySensor> humiditySensors = new HashMap<>();

	public HAPService() {
		System.out.println("Starting HAP");
		if (instance == null) {
			instance = this;
		}

		init();
		EventBusService.getEventBus().register(this);
	}

	public void addHumiditySensor(final HomekitRoot bridge) {
		System.out.println("Loading Humidity");
		@SuppressWarnings("unchecked")
		final List<Sensor> sensorList = em.createQuery("select s from Sensor s where s.sensorType='HUMIDITY'")
				.getResultList();

		if (sensorList != null) {
			for (final Sensor singleSensor : sensorList) {
				@SuppressWarnings("unchecked")
				final List<SensorData> latestDataList = em
						.createQuery("select sd from SensorData sd where sd.sensor=:sensor order by sd.dateTime desc")
						.setParameter("sensor", singleSensor).setMaxResults(1).getResultList();

				String name = replaceUmlaut(singleSensor.getSensorName());
				name += " in " + singleSensor.getRoom().getRoomName();

				System.out.println("Adding Sensor: " + name);

				final HAPHumiditySensor hapHumiditySensor = new HAPHumiditySensor(name, singleSensor.getId());

				if ((latestDataList != null) && !latestDataList.isEmpty()) {

					final SensorData sensorData = latestDataList.get(0);
					hapHumiditySensor.setHumidity(new Double(sensorData.getValue().replace(",", ".")));
				}

				getHumiditySensors().put(singleSensor.getId(), hapHumiditySensor);
				bridge.addAccessory(hapHumiditySensor);
			}

		}
	}

	public void addLightsToBridge(final HomekitRoot bridge) {
		System.out.println("Loading Lights");
		@SuppressWarnings("unchecked")
		final List<Switch> switchList = em.createQuery("select sw from Switch sw where sw.switchType='LIGHT'")
				.getResultList();

		if (switchList != null) {
			for (final Switch singleSwitch : switchList) {
				boolean status = false;
				if ("ON".equals(singleSwitch.getLatestStatus())) {
					status = true;
				}

				String name = replaceUmlaut(singleSwitch.getName());
				name += " in " + singleSwitch.getRoom().getRoomName();

				System.out.println("Adding Light: " + name);

				final HAPLight hapSwitch = new HAPLight(name, status, singleSwitch.getId());

				bridge.addAccessory(hapSwitch);
			}

		}
	}

	public void addSocketsToBridge(final HomekitRoot bridge) {
		System.out.println("Loading Switches");
		@SuppressWarnings("unchecked")
		final List<Switch> switchList = em.createQuery("select sw from Switch sw where sw.switchType='SOCKET'")
				.getResultList();

		if (switchList != null) {
			for (final Switch singleSwitch : switchList) {
				boolean status = false;
				if ("ON".equals(singleSwitch.getLatestStatus())) {
					status = true;
				}

				String name = replaceUmlaut(singleSwitch.getName());
				name += " in " + singleSwitch.getRoom().getRoomName();

				System.out.println("Adding Switch: " + name);

				final HAPSwitch hapSwitch = new HAPSwitch(name, status, singleSwitch.getId());

				bridge.addAccessory(hapSwitch);
			}

		}
	}

	public void addTemperatureSensor(final HomekitRoot bridge) {
		System.out.println("Loading Temperature");
		@SuppressWarnings("unchecked")
		final List<Sensor> sensorList = em.createQuery("select s from Sensor s where s.sensorType='TEMPERATURE'")
				.getResultList();

		if (sensorList != null) {
			for (final Sensor singleSensor : sensorList) {
				@SuppressWarnings("unchecked")
				final List<SensorData> latestDataList = em
						.createQuery("select sd from SensorData sd where sd.sensor=:sensor order by sd.dateTime desc")
						.setParameter("sensor", singleSensor).setMaxResults(1).getResultList();

				if ((latestDataList != null) && !latestDataList.isEmpty()) {
					final SensorData sensorData = latestDataList.get(0);

					String name = replaceUmlaut(singleSensor.getSensorName());
					name += " in " + singleSensor.getRoom().getRoomName();

					System.out.println("Adding Sensor: " + name);

					final HAPTemperatureSensor hapTemperature = new HAPTemperatureSensor(name, singleSensor.getId());
					hapTemperature.setTemperature(new Double(sensorData.getValue().replace(",", ".")));
					getTemperatureSensors().put(singleSensor.getId(), hapTemperature);
					bridge.addAccessory(hapTemperature);
				}
			}

		}
	}

	public void addWindowCovering(final HomekitRoot bridge) {
		final WindowBlindService windowBlindService = new WindowBlindService();
		final WindowBlindsList allWindowBlinds = windowBlindService.getAll();

		final List<WindowBlind> windowBlindsList = allWindowBlinds.getWindowBlinds();

		for (final WindowBlind windowBlind : windowBlindsList) {
			final int id = 3000 + windowBlind.getId().intValue();
			final String name = replaceUmlaut(windowBlind.getName());
			final HAPWindowBlind hapWindowBlind = new HAPWindowBlind(windowBlind, id, name);
			System.out.println("Adding Windowblind: " + id + " - " + hapWindowBlind.getLabel());

			bridge.addAccessory(hapWindowBlind);
		}
	}

	public Map<Long, HAPHumiditySensor> getHumiditySensors() {
		return humiditySensors;
	}

	public InetAddress getLocalAddress() {
		InetAddress address = null;

		try {
			final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				final NetworkInterface n = e.nextElement();
				final Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					final InetAddress i = ee.nextElement();
					final String hostAddress = i.getHostAddress();
					if (hostAddress.startsWith("192.168")) {
						address = i;
						System.out.println(hostAddress);
					}
				}
			}
		} catch (final SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return address;
	}

	/**
	 * @return the temperatureSensors
	 */
	public Map<Long, HAPTemperatureSensor> getTemperatureSensors() {
		return temperatureSensors;
	}

	// @Subscribe
	public void handleSensorDataChanged(final EventObject eventObject) {

		final Object eventData = eventObject.getData();
		if (eventData instanceof SensorData) {

			final SensorData sensorData = (SensorData) eventData;
			if ("TEMPERATURE".equals(sensorData.getSensor().getSensorType())) {

				final HAPTemperatureSensor hapTemperatureSensor = HAPService.getInstance().getTemperatureSensors()
						.get(sensorData.getSensor().getId());

				if (hapTemperatureSensor != null) {
					final double valueAsDouble = Double.parseDouble(sensorData.getValue().replace(",", "."));
					hapTemperatureSensor.setTemperature(new Double(valueAsDouble));
					final HomekitCharacteristicChangeCallback subscribeCallback = hapTemperatureSensor
							.getSubscribeCallback();
					if (subscribeCallback != null) {
						subscribeCallback.changed();
					}
				}
			}
		}
	}

	public void init() {
		try {
			em = EntityManagerService.getNewManager();

			final InetAddress inetAddress = getLocalAddress();
			final HomekitServer homekit = new HomekitServer(inetAddress, PORT);
			// AuthInfoService authInfo = new AuthInfoService();
			final HomekitAuthInfo authInfo = new HomekitAuthInfoImpl("032-45-154");
			bridge = homekit.createBridge(authInfo, "Haus", "CM, Inc.", "1.0", "111a2222be2341");
			// bridge.allowUnauthenticatedRequests(true);
			addLightsToBridge(bridge);
			addSocketsToBridge(bridge);
			addTemperatureSensor(bridge);
			// addWindowCovering(bridge);
			addHumiditySensor(bridge);

			System.out.println("Starting bridge");
			bridge.start();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void setHumiditySensors(final Map<Long, HAPHumiditySensor> humiditySensors) {
		this.humiditySensors = humiditySensors;
	}

	/**
	 * @param temperatureSensors
	 *            the temperatureSensors to set
	 */
	public void setTemperatureSensors(final Map<Long, HAPTemperatureSensor> temperatureSensors) {
		this.temperatureSensors = temperatureSensors;
	}

	public void stop() {
		// bridge.getAdvertiser().getJmdns().unregisterAllServices();
		bridge.stop();
	}
}
