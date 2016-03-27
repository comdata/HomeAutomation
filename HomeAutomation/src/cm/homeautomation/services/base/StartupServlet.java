package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import cm.homeautomation.hap.HAPService;
import cm.homeautomation.jeromq.server.JeroMQServer;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;
	private SchedulerThread schedulerThread;
	//private Server webSocketServer;
	private JeroMQServer jeroMQServer;

	public void init(ServletConfig config) throws ServletException {
		System.out.println("Starting scheduler");
		schedulerThread = SchedulerThread.getInstance();
		
		System.out.println("Starting HAP");
		HAPService hapService=HAPService.getInstance();

		jeroMQServer = new JeroMQServer();
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
	}
	
}
