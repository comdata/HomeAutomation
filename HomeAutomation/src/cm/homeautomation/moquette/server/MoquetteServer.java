package cm.homeautomation.moquette.server;

import java.util.Arrays;
import java.util.List;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.ClasspathConfig;
import io.moquette.server.config.IConfig;

import static java.util.Arrays.asList;

import java.io.IOException;

public class MoquetteServer {

	public MoquetteServer() {
		start();
	}

	static class PublisherListener extends AbstractInterceptHandler {

		@Override
		public void onPublish(InterceptPublishMessage msg) {
			System.out.println(
					"Received on topic: " + msg.getTopicName() + " content: " + new String(msg.getPayload().array()));
		}
	}

	private void start() {

		try {
			final IConfig classPathConfig = new ClasspathConfig();
			classPathConfig.setProperty("authenticator_class", "cm.homeautomation.moquette.server.MoquetteAuthenticator");

			final Server mqttBroker = new Server();
			List<? extends InterceptHandler> userHandlers = asList(new PublisherListener());
			mqttBroker.startServer(classPathConfig, userHandlers);

			System.out.println("Broker started press [CTRL+C] to stop");
			// Bind a shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					System.out.println("Stopping broker");
					mqttBroker.stopServer();
					System.out.println("Broker stopped");
				}
			});

			Thread.sleep(20000);
			System.out.println("Before self publish");
		} catch (IOException e) {

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
