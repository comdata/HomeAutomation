package cm.homeautomation.networkmonitor;

import org.apache.log4j.LogManager;

import cm.homeautomation.services.base.EventBusAnnotationInitializer;

public class NetworkScannerLocalRunner {

	public static void main(String[] args) throws InterruptedException {
		EventBusAnnotationInitializer eventBusAnnotationInitializer = new EventBusAnnotationInitializer();
		eventBusAnnotationInitializer.getInstances();
		String subnet = "192.168.1";

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			LogManager.getLogger(NetworkScannerLocalRunner.class).error(e);
			throw e;
		}

		LogManager.getLogger(NetworkScannerLocalRunner.class).debug("Triggering scan");

		NetworkScanner.getInstance().checkHosts(subnet);
	}

}
