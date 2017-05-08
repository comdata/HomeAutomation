package cm.homeautomation.pushnotificiation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;

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
			
			LogManager.getLogger(this.getClass()).info("Notification setup using authToken: "+authToken+" projectId: "+projectId+" url: "+notificationUrl);

			EventBusService.getEventBus().register(this);
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).info("Could not find pushpad properties!");
		}
	}



	
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
			
			LogManager.getLogger(this.getClass()).info("Preparing notification: "+message);

			// optional, defaults to the project icon
			// notification.iconUrl = notificationUrl;
			notification.iconUrl=iconUrl;
			// optional, drop the notification after this number of seconds if a
			// device is offline
			notification.ttl = 5*3600;

			try {

				// deliver to everyone
				notification.broadcast();
				LogManager.getLogger(this.getClass()).info("Notification sent.");
				
			} catch (DeliveryException e) {
				e.printStackTrace();
			}
		}
	}
}
