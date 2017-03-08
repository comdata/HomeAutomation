package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.eventbus.EventBusEndpoint;
import cm.homeautomation.eventbus.EventBusEndpointConfigurator;
import cm.homeautomation.hap.HAPService;
import cm.homeautomation.jeromq.server.JeroMQServer;
import cm.homeautomation.mdns.MDNSService;
import cm.homeautomation.moquette.server.MoquetteServer;
import cm.homeautomation.services.overview.OverviewEndPointConfiguration;
import cm.homeautomation.services.overview.OverviewWebSocket;
import cm.homeautomation.telegram.TelegramBotService;
import cm.homeautomation.transmission.TransmissionMonitor;
import cm.homeautomation.mqtt.client.MQTTReceiverClient;
import cm.homeautomation.networkMonitor.NetworkDeviceDatabaseUpdater;
import cm.homeautomation.pushnotificiation.WindowBlindNotificationService;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;
	private SchedulerThread schedulerThread;
	private JeroMQServer jeroMQServer;
	private OverviewWebSocket overviewEndpoint;
	private MQTTReceiverClient moquetteClient;
	private EventBusEndpoint eventBusEndpoint;
	private Thread mqttThread;
	private WindowBlindNotificationService windowBlindNotificationService;
	private NetworkDeviceDatabaseUpdater networkDeviceDatabaseUpdater;
	private MDNSService mdnsService;
	private TelegramBotService telegramBotService;
	private Thread telegramThread;

	public void init(ServletConfig config) throws ServletException {

		System.out.println("Starting scheduler");
		schedulerThread = SchedulerThread.getInstance();

		EventBusAnnotationInitializer eventBusAnnotationInitializer = new EventBusAnnotationInitializer();

		Runnable mqttClient = new Runnable() {
			public void run() {
				moquetteClient = new MQTTReceiverClient();
				moquetteClient.start();
			}
		};
		mqttThread = new Thread(mqttClient);
		mqttThread.start();

		jeroMQServer = new JeroMQServer();

		overviewEndpoint = (OverviewWebSocket) eventBusAnnotationInitializer.getInstances()
				.get(OverviewWebSocket.class);
		OverviewEndPointConfiguration.setOverviewEndpoint(overviewEndpoint);
		

		eventBusEndpoint = (EventBusEndpoint) eventBusAnnotationInitializer.getInstances().get(EventBusEndpoint.class);
		EventBusEndpointConfigurator.setEventBusEndpoint(eventBusEndpoint);
		
		mdnsService = new MDNSService();
		mdnsService.registerServices();

		Runnable telegramRunnable = new Runnable() {
			public void run() {
				telegramBotService = TelegramBotService.getInstance();
				telegramBotService.init();
			}
		};
		
		telegramThread = new Thread(telegramRunnable);
		telegramThread.start();
	}

	public void destroy() {
		try {
			SchedulerThread.getInstance().stopScheduler();
		} catch (Exception e) {

		}

		try {
			HAPService.getInstance().stop();
		} catch (Exception e) {

		}

		try {
			jeroMQServer.stop();
		} catch (Exception e) {

		}

		try {
			if (mqttThread != null) {
				mqttThread.stop();
			}
		} catch (Exception e) {

		}

		try {
			moquetteClient.stopServer();
		} catch (Exception e) {

		}

		try {
			windowBlindNotificationService.destroy();
		} catch (Exception e) {

		}
		
		try {
			if (mdnsService != null) {
				mdnsService.destroy();
			}
		} catch (Exception e) {

		}
	}

}
