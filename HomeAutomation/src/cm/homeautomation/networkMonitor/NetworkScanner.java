package cm.homeautomation.networkMonitor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

public class NetworkScanner {

	private static NetworkScanner networkScanner;

	/**
	 * check for the availability of a host
	 * 
	 * @param subnet
	 * @return
	 */
	public ArrayList<String> checkHosts(String subnet) {
		ArrayList<String> availableHosts = new ArrayList<String>();

		int timeout = 1000;
		for (int i = 1; i < 255; i++) {
			String host = subnet + "." + i;
			try {
				if (InetAddress.getByName(host).isReachable(timeout)) {
					System.out.println(host + " is reachable");
					availableHosts.add(host);
				}
			} catch (IOException e) {
			}
		}

		
		
		return availableHosts;
	}

	/**
	 * make this a singleton
	 * 
	 * @return
	 */
	public static NetworkScanner getInstance() {

		if (networkScanner == null) {
			networkScanner = new NetworkScanner();
		}
		return networkScanner;
	}

	/**
	 * provide interface for the scheduler to scan the network for hosts
	 * 
	 * @param args
	 */
	public static void scanNetwork(String[] args) {
		ArrayList<String> checkHosts = NetworkScanner.getInstance().checkHosts(args[0]);
		
		NetworkScanResult data=new NetworkScanResult();
		data.setHosts(checkHosts);
		EventBusService.getEventBus().post(new EventObject(data));
	}

}
