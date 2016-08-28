package cm.homeautomation.networkMonitor;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NetworkScanResult {

	private ArrayList<String> hosts;

	public void setHosts(ArrayList<String> hosts) {
		this.hosts = hosts;

	}

	public ArrayList<String> getHosts() {
		return hosts;
	}

}
