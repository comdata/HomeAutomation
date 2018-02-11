package cm.homeautomation.tradfri;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TradfriGateway implements Runnable {

	/**
	 * Gateway properties and constructor
	 */
	protected String gateway_ip;
	protected String security_key;
	protected int polling_rate = 15000;

	private boolean running = false;

	/**
	 * Logger to be used for all console outputs, errors and exceptions
	 */
	private final Logger logger = Logger.getLogger(TradfriGateway.class.getName());

	/**
	 * Observer pattern for asynchronous event notification
	 */
	private final ArrayList<TradfriGatewayListener> listners = new ArrayList<>();

	// Collection of bulbs registered on the gateway
	ArrayList<LightBulb> bulbs = new ArrayList<>();

	/**
	 * COAPS helpers to GET and SET on the IKEA Tradfri gateway using Californium
	 */
	private CoapEndpoint coap = null;

	public TradfriGateway() {

	}

	public TradfriGateway(final String gateway_ip, final String security_key) {
		this.gateway_ip = gateway_ip;
		this.security_key = security_key;
	}

	public void addTradfriGatewayListener(final TradfriGatewayListener l) {
		listners.add(l);
	}

	public void clearTradfriGatewayListener() {
		listners.clear();
	}

	protected boolean dicoverBulbs() {
		bulbs.clear();
		try {
			CoapResponse response = get(TradfriConstants.DEVICES);
			if (response == null) {
				return false;
			}
			final JSONArray devices = new JSONArray(response.getResponseText());
			for (final TradfriGatewayListener l : listners) {
				l.bulb_discovery_started(devices.length());
			}
			for (int i = 0; i < devices.length(); i++) {
				response = get(TradfriConstants.DEVICES + "/" + devices.getInt(i));
				if (response != null) {
					final JSONObject json = new JSONObject(response.getResponseText());
					if (json.has(TradfriConstants.TYPE)
							&& (json.getInt(TradfriConstants.TYPE) == TradfriConstants.TYPE_BULB)) {
						final LightBulb b = new LightBulb(json.getInt(TradfriConstants.INSTANCE_ID), this, response);
						bulbs.add(b);
						for (final TradfriGatewayListener l : listners) {
							l.bulb_discovered(b);
						}
					}
				}

			}
			for (final TradfriGatewayListener l : listners) {
				l.bulb_discovery_completed();
			}
		} catch (final JSONException e) {
			logger.log(Level.SEVERE, "Error parsing response from the Tradfri gateway", e);
			return false;

		}
		return true;
	}

	protected CoapResponse get(final String path) {
		Logger.getLogger(TradfriGateway.class.getName()).log(Level.INFO,
				"GET: " + "coaps://" + gateway_ip + "/" + path);
		final CoapClient client = new CoapClient("coaps://" + gateway_ip + "/" + path);
		client.setEndpoint(coap);
		final CoapResponse response = client.get(1);
		if (response == null) {
			logger.log(Level.SEVERE,
					"Connection to Gateway timed out, please check ip address or increase the ACK_TIMEOUT in the Californium.properties file");
		}
		return response;
	}

	public String getGateway_ip() {
		return gateway_ip;
	}

	public Logger getLogger() {
		return logger;
	}

	public int getPolling_rate() {
		return polling_rate;
	}

	public String getSecurity_key() {
		return security_key;
	}

	protected void initCoap() {
		final DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(); // new InetSocketAddress(0)
		builder.setPskStore(new StaticPskStore("", security_key.getBytes()));
		coap = new CoapEndpoint(new DTLSConnector(builder.build()), NetworkConfig.getStandard());
	}

	public boolean isRunning() {
		return running;
	}

	public void removeTradfriGatewayListener(final TradfriGatewayListener l) {
		listners.remove(l);
	}

	@Override
	public void run() {
		for (final TradfriGatewayListener l : listners) {
			l.gateway_initializing();
		}
		Logger.getLogger(TradfriGateway.class.getName()).log(Level.INFO, "Tradfri Gateway is initalizing...");
		initCoap();
		Logger.getLogger(TradfriGateway.class.getName()).log(Level.INFO, "Discovering Devices...");
		if (dicoverBulbs()) {
			Logger.getLogger(TradfriGateway.class.getName()).log(Level.INFO, "Discovered " + bulbs.size() + " Bulbs.");
			for (final TradfriGatewayListener l : listners) {
				l.gateway_started();
			}
			try {
				while (running) {
					Thread.sleep(getPolling_rate());
					Logger.getLogger(TradfriGateway.class.getName()).log(Level.INFO, "Polling bulbs status...");
					for (final TradfriGatewayListener l : listners) {
						l.polling_started();
					}
					final long before = System.currentTimeMillis();
					for (final LightBulb b : bulbs) {
						b.updateBulb();
						// System.out.println(b.toString());
					}
					final long after = System.currentTimeMillis();
					for (final TradfriGatewayListener l : listners) {
						l.polling_completed(bulbs.size(), (int) (after - before));
					}
				}
			} catch (final InterruptedException ex) {
				Logger.getLogger(TradfriGateway.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		running = false;
		for (final TradfriGatewayListener l : listners) {
			l.gateway_stoped();
		}
	}

	protected void set(final String path, final String payload) {
		Logger.getLogger(TradfriGateway.class.getName()).log(Level.INFO,
				"SET: " + "coaps://" + gateway_ip + "/" + path + " = " + payload);
		final CoapClient client = new CoapClient("coaps://" + gateway_ip + "/" + path);
		client.setEndpoint(coap);
		final CoapResponse response = client.put(payload, MediaTypeRegistry.TEXT_PLAIN);
		if ((response != null) && response.isSuccess()) {
			// System.out.println("Yay");
		} else {
			logger.log(Level.SEVERE, "Sending payload to " + "coaps://" + gateway_ip + "/" + path + " failed!");
		}
		client.shutdown();
	}

	public void setGateway_ip(final String gateway_ip) {
		this.gateway_ip = gateway_ip;
	}

	public void setPolling_rate(int polling_rate) {
		// between 1 and 60 seconds
		if (polling_rate < 1000) {
			polling_rate = 1000;
		} else if (polling_rate > 60000) {
			polling_rate = 60000;
		}
		this.polling_rate = polling_rate;
	}

	public void setSecurity_key(final String security_key) {
		this.security_key = security_key;
	}

	/**
	 * Gateway public API
	 */
	public void startTradfriGateway() {
		if (running) {
			return;
		}
		running = true;
		new Thread(this).start();
	}

	public void stopTradfriGateway() {
		running = false;
	}
}
