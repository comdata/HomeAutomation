package cm.homeautomation.jeromq.server;

import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class JeroMQServer {

	private Thread jeroMQServerThread;
	private JeroMQServerThread jeroMQServerRunnable;

	public JeroMQServer() {
		jeroMQServerRunnable = new JeroMQServerThread();
		jeroMQServerThread = new Thread(jeroMQServerRunnable);
		jeroMQServerThread.start();
	}
	
	public void stop() {
		jeroMQServerRunnable.stop();
		jeroMQServerThread.stop();
	}
	
}
