package cm.homeautomation.networkMonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

public class NetworkScanner {

	private static NetworkScanner networkScanner;
	private static Map<String, Boolean> runningScans=new HashMap<String, Boolean>();
	private HashMap<String, NetworkDevice> availableHosts;

	/**
	 * check for the availability of a host
	 * 
	 * @param subnet
	 * @return
	 */
	public HashMap<String, NetworkDevice> checkHosts(String subnet) {
		setAvailableHosts(new HashMap<String, NetworkDevice>());

		int timeout = 1000;
		for (int i = 1; i < 255; i++) {
			String host = subnet + "." + i;
			try {
				InetAddress currentHost = InetAddress.getByName(host);
				if (currentHost.isReachable(timeout)) {
					LogManager.getLogger(this.getClass()).info(host + " is reachable");

					String macFromArpCache = getMacFromArpCache(host);

					String key = host + "-" + macFromArpCache;
					if (!getAvailableHosts().keySet().contains(key)) {

						NetworkDevice device = new NetworkDevice();
						device.setIp(host);
						device.setHostname(currentHost.getHostName());

						device.setMac(macFromArpCache);

						getAvailableHosts().put(key, device);

						NetworkScannerHostFoundMessage newHostMessage = new NetworkScannerHostFoundMessage();
						newHostMessage.setHost(device);

						EventBusService.getEventBus().post(new EventObject(newHostMessage));
					}
				}
			} catch (IOException e) {
				LogManager.getLogger(this.getClass()).info(e);
			}
		}

		return getAvailableHosts();
	}

	/**
	 * Try to extract a hardware MAC address from a given IP address using the
	 * ARP cache (/proc/net/arp).<br>
	 * <br>
	 * We assume that the file has this structure:<br>
	 * <br>
	 * IP address HW type Flags HW address Mask Device 192.168.18.11 0x1 0x2
	 * 00:04:20:06:55:1a * eth0 192.168.18.36 0x1 0x2 00:22:43:ab:2a:5b * eth0
	 *
	 * @param ip
	 * @return the MAC from the ARP cache
	 */
	private String getMacFromArpCache(String ip) {
		if (ip == null)
			return null;
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader("/proc/net/arp");
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
				if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
					// Basic sanity check
					String mac = splitted[3];
					if (mac.matches("..:..:..:..:..:..")) {
						if ("00:00:00:00:00:00".equals(mac)) {
							return null;
						} else {

							return mac;
						}
					} else {
						return null;
					}
				}
			}
		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).info(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (IOException e) {
				LogManager.getLogger(this.getClass()).info(e);
			}
		}
		return null;
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
		String subnet = args[0];
		
		Boolean scanRunningObject = runningScans.get(subnet);
		
		if (scanRunningObject==null) {
			runningScans.put(subnet, new Boolean(false));
			scanRunningObject = runningScans.get(subnet);
		}
		
		if (!scanRunningObject.booleanValue()) {
			runningScans.put(subnet, new Boolean(true));

			
			HashMap<String, NetworkDevice> checkHosts = NetworkScanner.getInstance().checkHosts(subnet);

			NetworkScanResult data = new NetworkScanResult();
			data.setHosts(checkHosts);
			EventBusService.getEventBus().post(new EventObject(data));
			runningScans.put(subnet, new Boolean(false));
		}

	}

	public HashMap<String, NetworkDevice> getAvailableHosts() {
		return availableHosts;
	}

	public void setAvailableHosts(HashMap<String, NetworkDevice> availableHosts) {
		this.availableHosts = availableHosts;
	}

}
