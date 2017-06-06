package xyz.pushpad;

import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.telegram.TelegramBotService;

import javax.crypto.Mac;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.Executors;

public class Pushpad {
	public String authToken;
	public String projectId;

	private String notificationUrl;
	private String iconUrl;

	public Pushpad() {

		EventBusService.getEventBus().register(this);

		initialize("/home/hap/pushpad.properties");
	}

	/**
	 * do an unregister
	 */
	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	protected void initialize(String fileName) {
		try {
			Properties props = new Properties();
			File file = new File(fileName);
			FileReader fileReader = new FileReader(file);

			props.load(fileReader);

			authToken = props.getProperty("authToken");
			projectId = props.getProperty("projectId");
			notificationUrl = props.getProperty("notificationUrl");
			iconUrl = props.getProperty("iconUrl");

			LogManager.getLogger(this.getClass()).info("Notification setup using authToken: " + authToken
					+ " projectId: " + projectId + " url: " + notificationUrl);

			EventBusService.getEventBus().register(this);
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).info("Could not find pushpad properties!");
		}
	}

	public String signatureFor(String data) {
		SecretKeySpec signingKey = new SecretKeySpec(this.authToken.getBytes(), "HmacSHA1");
		String encoded = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			encoded = Base64.getEncoder().withoutPadding().encodeToString(rawHmac);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
		}

		return encoded;
	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleEvent(EventObject eventObject) {
		// try {
		if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
			HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventObject.getData();

			Notification notification = this.buildNotification(humanMessage.getTitle(), humanMessage.getMessageString(),
					notificationUrl);

			// optional, defaults to the project icon
			// notification.iconUrl = notificationUrl;
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

					} catch (DeliveryException e) {
						e.printStackTrace();
					}
				}
			});

		}
	}

	public String path() {
		return "https://pushpad.xyz/projects/" + this.projectId + "/subscription/edit";
	}

	public String pathFor(String uid) {
		String uidSignature = this.signatureFor(uid);
		return this.path() + "?uid=" + uid + "&uid_signature=" + uidSignature;
	}

	public Notification buildNotification(String title, String body, String targetUrl) {
		return new Notification(this, title, body, targetUrl);
	}
}
