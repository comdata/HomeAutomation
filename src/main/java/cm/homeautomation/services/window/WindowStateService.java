package cm.homeautomation.services.window;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Window;
import cm.homeautomation.entities.WindowState;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.window.WindowStateData;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;

@Path("window")
public class WindowStateService extends BaseService {

	
	private static WindowStateService instance;

	public WindowStateService() {
		instance=this;
	}
	
	public static WindowStateService getInstance() {
		if (instance==null) {
			new WindowStateService();
		}
		
		return instance;

	}
	
	@GET
	@Path("readAll")
	public List<WindowStateData> get() {

		final List<WindowStateData> windowStateList = new ArrayList<>();

		final EntityManager em = EntityManagerService.getNewManager();

		final List<WindowState> results = em.createQuery(
				"select ws from WindowState ws where ws.id in (select max(w.id) from WindowState w group by w.window) order by ws.timestamp desc", WindowState.class)
				.getResultList();

		for (final WindowState windowState : results) {
			final String mac = windowState.getMac();

			final WindowStateData windowStateData = new WindowStateData();

			windowStateData.setMac(windowState.getMac());
			windowStateData.setState(windowState.getState());
			windowStateData.setWindow(windowState.getWindow());
			
			windowStateData.setRoomName(windowState.getWindow().getRoom().getRoomName());
			windowStateData.setWindowName(windowState.getWindow().getName());

			windowStateData.setRoom(windowState.getWindow().getRoom());
			windowStateData.setDevice(DeviceService.getDeviceForMac(mac));
			windowStateData.setDate(windowState.getTimestamp());

			windowStateList.add(windowStateData);
		}

		em.close();

		return windowStateList;
	}

	@GET
	@Path("setState/{windowId}/{state}")
	public GenericStatus handleWindowState(@PathParam("windowId") Long windowId, @PathParam("state") String state) {

		synchronized (this) {

			LogManager.getLogger(this.getClass()).debug("window: " + windowId + " state: ---" + state + "---");

			final EntityManager em = EntityManagerService.getNewManager();

			final List<Window> resultList = em.createQuery("select w from Window w where w.id=:id", Window.class).setParameter("id", windowId)
					.getResultList();

			if ((resultList != null) && !resultList.isEmpty()) {
				final Window window = resultList.get(0);

				if (window.getStateSensor() == null) {
					addNewSensorToWindowIfMissing(em, window);

				}

				em.getTransaction().begin();

				state = getSanitizedState(state);


				saveWindowStateSensor(windowId, state, window);

				final WindowState windowState = createWIndowState(state, em, window);
				
				sendWindowStateEvent(window, windowState);

				em.getTransaction().commit();

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
				Sensors.getInstance().saveSensorData(sensorDataSaveRequest);
			} catch (SensorDataLimitViolationException e) {
				LogManager.getLogger(this.getClass()).error("window: " + windowId + " state: ---" + state + "---", e);
			}
		}
	}

	private void sendWindowStateEvent(final Window window, final WindowState windowState) {
		final WindowStateData windowStateData = new WindowStateData();

		windowStateData.setState(windowState.getState());
		windowStateData.setRoom(windowState.getWindow().getRoom());
		windowStateData.setWindow(window);

		final EventObject intervalEventObject = new EventObject(windowStateData);
		EventBusService.getEventBus().post(intervalEventObject);
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

	private void addNewSensorToWindowIfMissing(final EntityManager em, final Window window) {
		em.getTransaction().begin();
		final Sensor stateSensor = new Sensor();
		stateSensor.setRoom(window.getRoom());
		stateSensor.setSensorName(window.getName());
		stateSensor.setShowData(true);
		window.setStateSensor(stateSensor);
		em.persist(stateSensor);
		em.merge(window);
		em.getTransaction().commit();
	}

}
