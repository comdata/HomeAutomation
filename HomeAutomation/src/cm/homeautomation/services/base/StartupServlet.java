package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;
	private SensorDataThread sensorDataThread;
	private WeatherDataThread weatherDataThread;

	public void init(ServletConfig config) throws ServletException {
		sensorDataThread = new SensorDataThread();

		weatherDataThread = new WeatherDataThread();

		weatherDataThread.start();
		sensorDataThread.start();

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

	}
}
