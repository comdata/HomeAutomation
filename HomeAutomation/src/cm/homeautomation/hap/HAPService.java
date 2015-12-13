package cm.homeautomation.hap;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;
import com.beowulfe.hap.impl.jmdns.JmdnsHomekitAdvertiser;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;

public class HAPService {
	private static final int PORT = 9123;

	private static HAPService instance;

	private EntityManager em;

	private HomekitRoot bridge;
	private Map<Long, HAPTemperatureSensor> temperatureSensors=new HashMap<Long, HAPTemperatureSensor>();

	public HAPService() {
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		LoggerContext loggerContext = rootLogger.getLoggerContext();
		loggerContext.reset();

		PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
		logEncoder.setContext(loggerContext);
		logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
		logEncoder.start();

		ConsoleAppender logConsoleAppender = new ConsoleAppender();
		logConsoleAppender.setContext(loggerContext);
		logConsoleAppender.setName("console");
		logConsoleAppender.setEncoder(logEncoder);
		logConsoleAppender.start();

		init();
	}


	public void addTemperatureSensor(HomekitRoot bridge) {
		System.out.println("Loading Switches");
		List<Sensor> sensorList = em.createQuery("select s from Sensor s where s.sensorType='TEMPERATURE'").getResultList();

		if (sensorList != null) {
			for (Sensor singleSensor : sensorList) {
				List<SensorData> latestDataList = em
						.createQuery("select sd from SensorData sd where sd.sensor=:sensor order by sd.dateTime desc")
						.setParameter("sensor", singleSensor).setMaxResults(1).getResultList();

				SensorData sensorData = latestDataList.get(0);
				
				System.out.println("Adding Sensor: " + singleSensor.getSensorName());

				HAPTemperatureSensor hapTemperature = new HAPTemperatureSensor(singleSensor.getSensorName(), singleSensor.getId());
				hapTemperature.setTemperature(new Double(sensorData.getValue().replace(",", ".")));
				getTemperatureSensors().put(singleSensor.getId(), hapTemperature);
				bridge.addAccessory(hapTemperature);
			}

		}
	}
	
	public void addSwitchesToBridge(HomekitRoot bridge) {
		System.out.println("Loading Switches");
		List<Switch> switchList = em.createQuery("select sw from Switch sw").getResultList();

		if (switchList != null) {
			for (Switch singleSwitch : switchList) {
				boolean status = false;
				if ("ON".equals(singleSwitch.getLatestStatus())) {
					status = true;
				}

				System.out.println("Adding Switch: " + singleSwitch.getName());

				HAPSwitch hapSwitch = new HAPSwitch(singleSwitch.getName(), status, singleSwitch.getId());

				bridge.addAccessory(hapSwitch);
			}

		}
	}

	public InetAddress getLocalAddress() {
		InetAddress address=null;
		
		Enumeration e;
		try {
			e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					String hostAddress = i.getHostAddress();
					if (hostAddress.startsWith("192.168")) {
						address=i;
						System.out.println(hostAddress);
					}
				}
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return address;
	}

	public void init() {
		try {
			em = EntityManagerService.getNewManager();

			InetAddress inetAddress = getLocalAddress();
			HomekitServer homekit = new HomekitServer(inetAddress, PORT);
			AuthInfoService authInfo = new AuthInfoService();
			bridge = homekit.createBridge(authInfo, "Haus", "CM, Inc.", "V1", "111a2222be2341");
			//bridge.allowUnauthenticatedRequests(true);
			addSwitchesToBridge(bridge);
			addTemperatureSensor(bridge);

			System.out.println("Starting bridge");
			bridge.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		JmdnsHomekitAdvertiser.getJmdns().unregisterAllServices();
		bridge.stop();
	}

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
	 * @param instance
	 *            the instance to set
	 */
	public static void setInstance(HAPService instance) {
		HAPService.instance = instance;
	}


	/**
	 * @return the temperatureSensors
	 */
	public Map<Long, HAPTemperatureSensor> getTemperatureSensors() {
		return temperatureSensors;
	}


	/**
	 * @param temperatureSensors the temperatureSensors to set
	 */
	public void setTemperatureSensors(Map<Long, HAPTemperatureSensor> temperatureSensors) {
		this.temperatureSensors = temperatureSensors;
	}
}
