package cm.homeautomation.jeromq.server;

public class JeroMQServer {

	private Thread jeroMQServerThread;

	public JeroMQServer() {
		jeroMQServerThread = new Thread(new JeroMQServerThread());
		jeroMQServerThread.start();
	}
	
	public void stop() {
		jeroMQServerThread.stop();;
	}
	
}
