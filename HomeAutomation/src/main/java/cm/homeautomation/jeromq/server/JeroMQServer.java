package cm.homeautomation.jeromq.server;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class JeroMQServer {

	private Thread jeroMQServerThread;
	private JeroMQServerThread jeroMQServerRunnable;

	public JeroMQServer() {
		final boolean enableJeroMQ = Boolean
				.parseBoolean(ConfigurationService.getConfigurationProperty("jeromq", "enabled"));

		if (enableJeroMQ) {

			jeroMQServerRunnable = new JeroMQServerThread();
			jeroMQServerThread = new Thread(jeroMQServerRunnable);
			jeroMQServerThread.start();
		}
	}

	public void stop() {
		jeroMQServerRunnable.stop();
		jeroMQServerThread.stop();
	}

}
