package cm.homeautomation.mdns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.JmDNSImpl;

public class MDNSService {

	private JmDNS jmdns;

	public MDNSService() {
		try {
			jmdns = JmDNS.create(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void registerServices() {
		try {
			// Register a service
			ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "HomeAutomation", 8080,
					"path=index.html");
			registerService(serviceInfo);

			ServiceInfo mqttService = ServiceInfo.create("_mqtt._tcp.local.", "HomeAutomation", 1883,
					"path=index.html");
			registerService(mqttService);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void registerService(ServiceInfo serviceInfo) throws IOException {
		jmdns.registerService(serviceInfo);
	}

	public void destroy() {
		jmdns.unregisterAllServices();
	}

}
