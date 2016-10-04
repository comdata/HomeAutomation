package cm.homeautomation.mdns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class MDNSService {

	private JmDNS jmdns;

	public MDNSService() {
		try {
			jmdns = JmDNS.create(this.getIp());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private InetAddress getIp() {
		try {
			String localhosts = InetAddress.getLocalHost().getHostAddress();

			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {

					InetAddress i = ee.nextElement();
					String hostAddress = i.getHostAddress();

					if (hostAddress.equals(localhosts)) {
						System.out.println(hostAddress);
						return i;
					}
				}
			}
		} catch (SocketException e) {

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public void registerServices() {
		try {
			// Register a service
			ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "HomeAutomation", 8080,
					"path=index.html");
			registerService(serviceInfo);

			ServiceInfo mqttService = ServiceInfo.create("_mqtt._tcp.local.", "HomeAutomation", 1883,
					"/sensordata");
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
