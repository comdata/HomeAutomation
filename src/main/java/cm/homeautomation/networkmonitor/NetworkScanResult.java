package cm.homeautomation.networkmonitor;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.NetworkDevice;

@XmlRootElement
public class NetworkScanResult {

	private Map<String, NetworkDevice> hosts;

	public void setHosts(Map<String, NetworkDevice> checkHosts) {
		this.hosts = checkHosts;

	}

	public Map<String, NetworkDevice> getHosts() {
		return hosts;
	}

}
