package cm.homeautomation.sensors.window;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.PowerMeterPing;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowState;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.PowerMeterData;
import cm.homeautomation.sensors.WindowSensorData;

/**
 * receiver power meter data and save it to the database
 * 
 * @author christoph
 *
 */
public class WindowStateSensor {

	private EntityManager em;

	public WindowStateSensor() {
		em = EntityManagerService.getNewManager();
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleWindowState(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof WindowSensorData) {

			WindowSensorData windowSensorData = (WindowSensorData) data;
			String mac = windowSensorData.getMac();
			Room room = DeviceService.getRoomForMac(mac);

			WindowState windowState = new WindowState();
			windowState.setRoom(room);
			windowState.setState(windowSensorData.getState());
			windowState.setTimestamp(new Date());
			windowState.setMac(mac);

			synchronized (this) {
				em.getTransaction().begin();
				em.merge(windowState);
				em.getTransaction().commit();
			}

			WindowStateData windowStateData = new WindowStateData();
			windowStateData.setMac(mac);
			windowStateData.setState(windowSensorData.getState());
			windowStateData.setRoom(room);

			EventObject intervalEventObject = new EventObject(windowStateData);
			EventBusService.getEventBus().post(intervalEventObject);

		}
	}

}
