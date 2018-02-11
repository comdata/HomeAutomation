package cm.homeautomation.networkMonitor;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.NetworkDevice;

@XmlRootElement
public class NetworkScanResult {

	private HashMap<String, NetworkDevice> hosts;

	public void setHosts(HashMap<String, NetworkDevice> checkHosts) {
		this.hosts = checkHosts;

	}

	public HashMap<String, NetworkDevice> getHosts() {
		return hosts;
	}

}
