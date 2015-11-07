package cm.homeautomation.services.base;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import it.sauronsoftware.cron4j.Scheduler;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;
	private SensorDataThread sensorDataThread;
	private WeatherDataThread weatherDataThread;
	private SchedulerThread schedulerThread;


	public void init(ServletConfig config) throws ServletException {
		sensorDataThread = new SensorDataThread();

		weatherDataThread = new WeatherDataThread();
		schedulerThread = SchedulerThread.getInstance();

		weatherDataThread.start();
		sensorDataThread.start();
		schedulerThread.start();

		
	
	}

	public void destroy() {
		try {
			weatherDataThread.stopThread();
		} catch (Exception e) {

		}
		try {
			sensorDataThread.stopThread();
		} catch (Exception e) {

		}
		
		try {
			schedulerThread.stopThread();
		} catch (Exception e) {

		}

	}
}
