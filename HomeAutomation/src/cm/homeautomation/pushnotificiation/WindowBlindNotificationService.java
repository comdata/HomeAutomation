package cm.homeautomation.pushnotificiation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.windowblind.WindowBlindStatus;
import xyz.pushpad.DeliveryException;
import xyz.pushpad.Notification;
import xyz.pushpad.Pushpad;

public class WindowBlindNotificationService {

	private String authToken;
	private String projectId;
	private String notificationUrl;
	private String iconUrl;

	public WindowBlindNotificationService() {
		initialize("/home/hap/pushpad.properties");
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
			
			System.out.println("Notification setup using authToken: "+authToken+" projectId: "+projectId+" url: "+notificationUrl);

			EventBusService.getEventBus().register(this);
		} catch (IOException e) {
			System.out.println("Could not find pushpad properties!");
		}
	}

	/**
	 * do an unregister
	 */
	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe
	public void handleWindowBlindChange(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof WindowBlindStatus) {
			WindowBlindStatus windowBlindStatus = (WindowBlindStatus) data;

			Pushpad pushpad = new Pushpad(authToken, projectId);

			String name = windowBlindStatus.getWindowBlind().getName();
			String message = "Window Blind: " + name + " changed status to: "
					+ windowBlindStatus.getWindowBlind().getCurrentValue() + " % open.";
			Notification notification = pushpad.buildNotification("Window Blind ("+name+") changed position", message,
					notificationUrl);
			
			System.out.println("Preparing notification: "+message);

			// optional, defaults to the project icon
			// notification.iconUrl = notificationUrl;
			notification.iconUrl=iconUrl;
			// optional, drop the notification after this number of seconds if a
			// device is offline
			notification.ttl = 604800;

			try {

				// deliver to everyone
				notification.broadcast();
				System.out.println("Notification sent.");
				
			} catch (DeliveryException e) {
				e.printStackTrace();
			}
		}
	}
}
