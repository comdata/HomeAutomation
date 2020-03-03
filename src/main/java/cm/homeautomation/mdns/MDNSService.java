package cm.homeautomation.mdns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class MDNSService {

	private JmDNS jmdns;

	public MDNSService() {
		try {
			final boolean enableJMDNS = Boolean
					.parseBoolean(ConfigurationService.getConfigurationProperty("jmdns", "enabled"));

			if (enableJMDNS) {
				jmdns = JmDNS.create(this.getIp());
				this.registerServices();
			}
		} catch (final UnknownHostException e) {
			LogManager.getLogger(this.getClass()).error(e);
		} catch (final IOException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

	public void destroy() {
		jmdns.unregisterAllServices();
	}

	private InetAddress getIp() {
		try {
			final String localhosts = InetAddress.getLocalHost().getHostAddress();

			final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				final NetworkInterface n = e.nextElement();
				final Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {

					final InetAddress i = ee.nextElement();
					final String hostAddress = i.getHostAddress();

					if (hostAddress.equals(localhosts)) {
						System.out.println(hostAddress);
						return i;
					}
				}
			}
		} catch (final SocketException e) {
			LogManager.getLogger(this.getClass()).error(e);
		} catch (final UnknownHostException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
		return null;
	}

	private void registerService(ServiceInfo serviceInfo) throws IOException {
		jmdns.registerService(serviceInfo);
	}

	public void registerServices() {
		try {
			// Register a service
			final ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "HomeAutomation", 8080,
					"path=index.html");
			registerService(serviceInfo);

			final ServiceInfo mqttService = ServiceInfo.create("_mqtt._tcp.local.", "HomeAutomation", 1883,
					"/sensordata");
			registerService(mqttService);
		} catch (final IOException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

}
