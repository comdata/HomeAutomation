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
	private Scheduler s;

	public void init(ServletConfig config) throws ServletException {
		sensorDataThread = new SensorDataThread();

		weatherDataThread = new WeatherDataThread();

		weatherDataThread.start();
		sensorDataThread.start();

		
		s = new Scheduler();
		s.scheduleFile(new File("schedule.cron"));
		s.start();
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
			s.stop();
		} catch (Exception e) {

		}

	}
}
