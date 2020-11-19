package cm.homeautomation.sensors.window;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowState;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.WindowSensorData;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * receiver power meter data and save it to the database
 *
 * @author christoph
 *
 */
@Singleton
public class WindowStateSensor {

	@Inject
	EntityManager em;
	
	@Inject
	ConfigurationService configurationService;
	
	@Inject
	DeviceService deviceService;
	
	@Inject
	EventBus bus;

	public WindowStateSensor() {
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handleWindowState(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof WindowSensorData) {

			final WindowSensorData windowSensorData = (WindowSensorData) data;
			final String mac = windowSensorData.getMac();
			final Room room = deviceService.getRoomForMac(mac);

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
			bus.publish("EventObject", intervalEventObject);

		}
	}

}
