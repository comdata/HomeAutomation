package cm.homeautomation.networkmonitor;

import cm.homeautomation.services.base.EventBusAnnotationInitializer;

public class NetworkScannerLocalRunner {

	public static void main(String[] args) {
		EventBusAnnotationInitializer eventBusAnnotationInitializer = new EventBusAnnotationInitializer();
		String subnet = "192.168.1";
		String[] networks={subnet};
		
		//NetworkDeviceDatabaseUpdater networkDeviceDatabaseUpdater = new NetworkDeviceDatabaseUpdater();

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Triggering scan");
		
		NetworkScanner.getInstance().checkHosts(subnet);

		
		
	}

}
