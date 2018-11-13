package xyz.pushpad;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class Pushpad {
	public String authToken;
	public String projectId;

	private String notificationUrl;
	private String iconUrl;
	private boolean enabled = true;

	public Pushpad() {
		EventBusService.getEventBus().register(this);

		enabled = Boolean.parseBoolean(ConfigurationService.getConfigurationProperty("pushpad", "enabled"));

		initialize("/home/hap/pushpad.properties");
	}

	public Notification buildNotification(String title, final String body, final String targetUrl) {

		if (title == null) {
			title = "";
		}

		return new Notification(this, title, body, targetUrl);
	}

	/**
	 * do an unregister
	 */
	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe
	public void handleEvent(final EventObject eventObject) {
		if (enabled) {
			if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
				final HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventObject
						.getData();

				final Notification notification = this.buildNotification(humanMessage.getTitle(),
						humanMessage.getMessageString(), notificationUrl);

				notification.iconUrl = iconUrl;
				// optional, drop the notification after this number of seconds if a
				// device is offline
				notification.ttl = 5 * 3600;

				Executors.newSingleThreadExecutor().execute(new Runnable() {
					@Override
					public void run() {
						try {

							// deliver to everyone
							notification.broadcast();
							LogManager.getLogger(this.getClass()).info("Notification sent.");

						} catch (final DeliveryException e) {
							LogManager.getLogger(this.getClass()).error(e);
						}
					}
				});

			}
		}
	}

	protected void initialize(final String fileName) {
		try {
			final Properties props = new Properties();
			final File file = new File(fileName);
			final FileReader fileReader = new FileReader(file);

			props.load(fileReader);

			authToken = props.getProperty("authToken");
			projectId = props.getProperty("projectId");
			notificationUrl = props.getProperty("notificationUrl");
			iconUrl = props.getProperty("iconUrl");

			LogManager.getLogger(this.getClass()).info("Notification setup using authToken: " + authToken
					+ " projectId: " + projectId + " url: " + notificationUrl);

			EventBusService.getEventBus().register(this);
		} catch (final IOException e) {
			LogManager.getLogger(this.getClass()).info("Could not find pushpad properties!");
		}
	}

	public String path() {
		return "https://pushpad.xyz/projects/" + this.projectId + "/subscription/edit";
	}

	public String pathFor(final String uid) {
		final String uidSignature = this.signatureFor(uid);
		return this.path() + "?uid=" + uid + "&uid_signature=" + uidSignature;
	}

	public String signatureFor(final String data) {
		final SecretKeySpec signingKey = new SecretKeySpec(this.authToken.getBytes(), "HmacSHA1");
		String encoded = null;
		try {
			final Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			final byte[] rawHmac = mac.doFinal(data.getBytes());
			encoded = Base64.getEncoder().withoutPadding().encodeToString(rawHmac);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

		return encoded;
	}
}
