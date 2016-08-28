package cm.homeautomation.networkMonitor;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.NetworkDevice;

@XmlRootElement
public class NetworkScannerHostFoundMessage {

	private NetworkDevice host;

	public void setHost(NetworkDevice device) {
		this.host = device;

	}

	public NetworkDevice getHost() {
		return host;
	}

}
