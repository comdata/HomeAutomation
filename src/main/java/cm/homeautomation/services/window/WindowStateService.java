package cm.homeautomation.services.window;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Window;
import cm.homeautomation.entities.WindowState;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.window.WindowStateData;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import io.vertx.core.eventbus.EventBus;

@Path("window")
public class WindowStateService extends BaseService {
	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Inject
	DeviceService deviceService;

	@Inject
	Sensors sensors;

	private static WindowStateService instance;

	public WindowStateService() {
		instance = this;
	}

	public static WindowStateService getInstance() {
		if (instance == null) {
			new WindowStateService();
		}

		return instance;

	}

	@GET
	@Path("readAll")
	public List<WindowStateData> get() {

		final List<WindowStateData> windowStateList = new ArrayList<>();

		final List<WindowState> results = em.createQuery(
				"select ws from WindowState ws where ws.id in (select max(w.id) from WindowState w group by w.window) order by ws.timestamp desc",
				WindowState.class).getResultList();

		for (final WindowState windowState : results) {
			final String mac = windowState.getMac();

			final WindowStateData windowStateData = new WindowStateData();

			windowStateData.setMac(windowState.getMac());
			windowStateData.setState(windowState.getState());
			windowStateData.setWindow(windowState.getWindow());

			windowStateData.setRoomName(windowState.getWindow().getRoom().getRoomName());
			windowStateData.setWindowName(windowState.getWindow().getName());

			windowStateData.setRoom(windowState.getWindow().getRoom());
			windowStateData.setDevice(deviceService.getDeviceForMac(mac));
			windowStateData.setDate(windowState.getTimestamp());

			windowStateList.add(windowStateData);
		}

		return windowStateList;
	}

	@GET
	@Path("setState/{windowId}/{state}")
	@Transactional
	public GenericStatus handleWindowState(@PathParam("windowId") Long windowId, @PathParam("state") String state) {

		synchronized (this) {

//			LogManager.getLogger(this.getClass()).debug("window: " + windowId + " state: ---" + state + "---");

			final List<Window> resultList = em.createQuery("select w from Window w where w.id=:id", Window.class)
					.setParameter("id", windowId).getResultList();

			if ((resultList != null) && !resultList.isEmpty()) {
				final Window window = resultList.get(0);

				if (window.getStateSensor() == null) {
					addNewSensorToWindowIfMissing(em, window);

				}

				state = getSanitizedState(state);

				saveWindowStateSensor(windowId, state, window);

				final WindowState windowState = createWIndowState(state, em, window);

				sendWindowStateEvent(window, windowState);

			}

		}

		return new GenericStatus(true);
	}

	private String getSanitizedState(String state) {
		state = (state != null) ? state.trim() : "";

		if (state.length() >= 4) {
			state = state.substring(0, 4);
		}
		return state;
	}

	private void saveWindowStateSensor(Long windowId, String state, final Window window) {
		final Sensor stateSensor = window.getStateSensor();
		if (stateSensor != null) {
			final SensorDataSaveRequest sensorDataSaveRequest = createSensorDataSaveRequest(state, stateSensor);

			try {
				sensors.saveSensorData(sensorDataSaveRequest);
			} catch (SensorDataLimitViolationException | SecurityException | IllegalStateException | RollbackException
					| HeuristicMixedException | HeuristicRollbackException | SystemException
					| NotSupportedException e) {
//				LogManager.getLogger(this.getClass()).error("window: " + windowId + " state: ---" + state + "---", e);
			}
		}
	}

	private void sendWindowStateEvent(final Window window, final WindowState windowState) {
		final WindowStateData windowStateData = new WindowStateData();

		windowStateData.setState(windowState.getState());
		windowStateData.setRoom(windowState.getWindow().getRoom());
		windowStateData.setWindow(window);

		final EventObject intervalEventObject = new EventObject(windowStateData);
		bus.publish("EventObject", intervalEventObject);
	}

	private WindowState createWIndowState(String state, final EntityManager em, final Window window) {
		final WindowState windowState = new WindowState();

		windowState.setWindow(window);
		windowState.setState(("open".equals(state) ? 1 : 0));
		windowState.setTimestamp(new Date());
		em.persist(windowState);
		return windowState;
	}

	private SensorDataSaveRequest createSensorDataSaveRequest(String state, final Sensor stateSensor) {
		final SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
		sensorDataSaveRequest.setSensorId(stateSensor.getId());
		final SensorData sensorData = new SensorData();
		sensorData.setValue("" + ("open".equals(state) ? 1 : 0));
		sensorData.setSensor(stateSensor);
		sensorData.setDateTime(new Date());

		sensorDataSaveRequest.setSensorData(sensorData);
		return sensorDataSaveRequest;
	}

	@Transactional
	private void addNewSensorToWindowIfMissing(final EntityManager em, final Window window) {

		final Sensor stateSensor = new Sensor();
		stateSensor.setRoom(window.getRoom());
		stateSensor.setSensorName(window.getName());
		stateSensor.setShowData(true);
		window.setStateSensor(stateSensor);
		em.persist(stateSensor);
		em.merge(window);

	}

}
