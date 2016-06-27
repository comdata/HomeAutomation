package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import cm.homeautomation.eventbus.EventBus;
import cm.homeautomation.hap.HAPService;
import cm.homeautomation.jeromq.server.JeroMQServer;
import cm.homeautomation.moquette.server.MoquetteServer;
import cm.homeautomation.services.overview.OverviewEndPointConfiguration;
import cm.homeautomation.services.overview.OverviewWebSocket;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;
	private SchedulerThread schedulerThread;
	// private Server webSocketServer;
	private JeroMQServer jeroMQServer;
	private OverviewWebSocket overviewEndpoint;
	private MoquetteServer moquetteServer;

	public void init(ServletConfig config) throws ServletException {
		EventBus.getInstance();
		
		System.out.println("Starting scheduler");
		schedulerThread = SchedulerThread.getInstance();

		System.out.println("Starting HAP");
		HAPService hapService = HAPService.getInstance();

		moquetteServer = new MoquetteServer();
		
		jeroMQServer = new JeroMQServer();

		try {
			OverviewEndPointConfiguration overviewEndPointConfiguration = new OverviewEndPointConfiguration();

			overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);

			overviewEndpoint.start();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			EventBus.getInstance().close();
		} catch (Exception e) {

		}
	}

}
