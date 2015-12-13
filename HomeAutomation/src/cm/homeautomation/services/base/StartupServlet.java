package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import cm.homeautomation.hap.HAPService;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;
	private SchedulerThread schedulerThread;
	//private Server webSocketServer;

	public void init(ServletConfig config) throws ServletException {
		System.out.println("Starting scheduler");
		schedulerThread = SchedulerThread.getInstance();
		
		System.out.println("Starting HAP");
		HAPService hapService=HAPService.getInstance();


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
	}
	
}
