
package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.eventbus.EventBusEndpoint;
import cm.homeautomation.eventbus.EventBusEndpointConfigurator;
import cm.homeautomation.hap.HAPService;
import cm.homeautomation.jeromq.server.JeroMQServer;
import cm.homeautomation.mdns.MDNSService;
import cm.homeautomation.mqtt.client.MQTTReceiverClient;
import cm.homeautomation.pushnotificiation.WindowBlindNotificationService;
import cm.homeautomation.services.overview.OverviewEndPointConfiguration;
import cm.homeautomation.services.overview.OverviewWebSocket;
import cm.homeautomation.telegram.TelegramBotService;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;
	private JeroMQServer jeroMQServer;
	private OverviewWebSocket overviewEndpoint;
	private MQTTReceiverClient moquetteClient;
	private EventBusEndpoint eventBusEndpoint;
	private Thread mqttThread;
	private WindowBlindNotificationService windowBlindNotificationService;
	private MDNSService mdnsService;
	private TelegramBotService telegramBotService;
	private Thread telegramThread;
	private StartupAnnotationInitializer startupAnnotationInitializer;

	@Override
	public void init(ServletConfig config) throws ServletException {

		LogManager.getLogger(this.getClass()).info("Starting scheduler");
		
		startupAnnotationInitializer = new StartupAnnotationInitializer();
		startupAnnotationInitializer.start();
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
				mqttThread.interrupt();
			}
		} catch (Exception e) {

		}

		try {
			moquetteClient.stopServer();
		} catch (Exception e) {

		}

		
		try {
			if (mdnsService != null) {
				mdnsService.destroy();
			}
		} catch (Exception e) {

		}
		
		startupAnnotationInitializer.disposeInstances();
	}

}
