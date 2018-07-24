
package cm.homeautomation.services.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;

/**
 * calls the startup annotation initializer and bridges from servlet
 * 
 * @author christoph
 *
 */
public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131323703998785803L;

	private StartupAnnotationInitializer startupAnnotationInitializer;

	@Override
	public void init(ServletConfig config) throws ServletException {

		LogManager.getLogger(this.getClass()).info("Starting scheduler");
		
		startupAnnotationInitializer = new StartupAnnotationInitializer();
		startupAnnotationInitializer.start();
	}

	public void destroy() {
		
		
		startupAnnotationInitializer.disposeInstances();
	}

}
