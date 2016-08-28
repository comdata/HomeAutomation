package cm.homeautomation.networkMonitor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NetworkScannerHostFoundMessage {

	private String host;

	public void setHost(String host) {
		this.host = host;

	}

	public String getHost() {
		return host;
	}

}
