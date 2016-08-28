package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.eventbus.EventBusEndpoint;
import cm.homeautomation.eventbus.EventBusEndpointConfigurator;
import cm.homeautomation.hap.HAPService;
import cm.homeautomation.jeromq.server.JeroMQServer;
import cm.homeautomation.moquette.server.MoquetteServer;
import cm.homeautomation.services.overview.OverviewEndPointConfiguration;
import cm.homeautomation.services.overview.OverviewWebSocket;
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
	// private Server webSocketServer;
	private JeroMQServer jeroMQServer;
	private OverviewWebSocket overviewEndpoint;
	private MQTTReceiverClient moquetteClient;
	private EventBusEndpoint eventBusEndpoint;
	private Thread mqttThread;
	private TransmissionMonitor transmissionMonitor;
	private WindowBlindNotificationService windowBlindNotificationService;
	private NetworkDeviceDatabaseUpdater networkDeviceDatabaseUpdater;

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
		OverviewEndPointConfiguration overviewEndPointConfiguration = new OverviewEndPointConfiguration();
		overviewEndPointConfiguration.setOverviewEndpoint(overviewEndpoint);

		eventBusEndpoint = (EventBusEndpoint) eventBusAnnotationInitializer.getInstances().get(EventBusEndpoint.class);
		EventBusEndpointConfigurator eventBusEndPointConfiguration = new EventBusEndpointConfigurator();
		eventBusEndPointConfiguration.setEventBusEndpoint(eventBusEndpoint);

		transmissionMonitor = new TransmissionMonitor();
		transmissionMonitor.start();

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
			transmissionMonitor.stopMonitor();
		} catch (Exception e) {

		}

		windowBlindNotificationService.destroy();
	}

}
