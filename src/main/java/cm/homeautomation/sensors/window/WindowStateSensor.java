package cm.homeautomation.sensors.window;

import java.util.Date;

import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowState;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.WindowSensorData;

/**
 * receiver power meter data and save it to the database
 *
 * @author christoph
 *
 */
public class WindowStateSensor {

	private final EntityManager em;

	public WindowStateSensor() {
		em = EntityManagerService.getNewManager();
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void handleWindowState(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof WindowSensorData) {

			final WindowSensorData windowSensorData = (WindowSensorData) data;
			final String mac = windowSensorData.getMac();
			final Room room = DeviceService.getRoomForMac(mac);

			final WindowState windowState = new WindowState();
			windowState.setState(windowSensorData.getState());
			windowState.setTimestamp(new Date());
			windowState.setMac(mac);

			synchronized (this) {
				em.getTransaction().begin();
				em.merge(windowState);
				em.getTransaction().commit();
			}

			final WindowStateData windowStateData = new WindowStateData();
			windowStateData.setMac(mac);
			windowStateData.setState(windowSensorData.getState());
			windowStateData.setRoom(room);

			final EventObject intervalEventObject = new EventObject(windowStateData);
			EventBusService.getEventBus().post(intervalEventObject);

		}
	}

}
