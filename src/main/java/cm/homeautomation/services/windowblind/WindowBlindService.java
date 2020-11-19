package cm.homeautomation.services.windowblind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.base.HTTPHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Startup
@ApplicationScoped
@Path("windowBlinds/")
public class WindowBlindService extends BaseService {
	@Inject
	MQTTSender mqttSender;

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	private Map<Long, List<WindowBlind>> windowBlindList;
	private Map<Long, WindowBlind> windowBlindMap;

	private static final String DIMVALUE = "{DIMVALUE}";

	void startup(@Observes StartupEvent event) {
		initWindowBlindList();

	}

	public enum DimDirection {
		UP, DOWN
	}

	/**
	 * call this method to perform a calibration of the window blind
	 *
	 * @param args
	 */
	public static synchronized void cronPerformCalibration(String[] args) {
		final String windowBlindId = args[0];

		new WindowBlindService().performCalibration(Long.valueOf(windowBlindId));
	}

	/**
	 * call this method to set a specific dim value
	 *
	 * @param args
	 */
	public static synchronized void cronSetDim(String[] args) {
		final String windowBlindId = args[0];
		final String dimValue = args[1];

		new WindowBlindService().setDim(Long.valueOf(windowBlindId), dimValue);
	}

	@GET
	@Path("getAll")
	public WindowBlindsList getAll() {
		final WindowBlindsList windowBlindsList = new WindowBlindsList();

		em.getTransaction().begin();

		final Set<Long> windowBlindIds = windowBlindMap.keySet();

		if (windowBlindIds != null) {
			for (final Long windowBlindId : windowBlindIds) {
				windowBlindsList.getWindowBlinds().add(windowBlindMap.get(windowBlindId));
			}
		}

		em.getTransaction().commit();

		return windowBlindsList;
	}

	@GET
	@Path("forRoom/{roomId}")
	public WindowBlindsList getAllForRoom(@PathParam("roomId") Long roomId) {
		final WindowBlindsList windowBlindsList = new WindowBlindsList();

		em.getTransaction().begin();

		final List<WindowBlind> windowBlinds = windowBlindList.get(roomId);

		if ((windowBlinds != null) && !windowBlinds.isEmpty()) {
			windowBlindsList.getWindowBlinds().addAll(windowBlinds);

			// create and entry for all at once
			final WindowBlind allAtOnce = new WindowBlind();
			allAtOnce.setType(WindowBlind.ALL_AT_ONCE);
			allAtOnce.setName("Alle");
			allAtOnce.setCurrentValue(windowBlindsList.getWindowBlinds().get(0).getCurrentValue());
			allAtOnce.setRoom(windowBlindsList.getWindowBlinds().get(0).getRoom());
			windowBlindsList.getWindowBlinds().add(allAtOnce);

		}

		em.getTransaction().commit();

		return windowBlindsList;
	}

	public void performCalibration(Long windowBlindId) {

		final WindowBlind windowBlind = em.find(WindowBlind.class, windowBlindId);

		if (windowBlind != null) {
			final String calibrationUrl = windowBlind.getCalibrationUrl();

			if ((calibrationUrl != null) && !("".equals(calibrationUrl))) {
				HTTPHelper.performHTTPRequest(calibrationUrl);
			}
		}

	}

	@GET
	@Path("setDim/{windowBlind}/{value}")
	public GenericStatus setDim(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value) {
		setDim(windowBlindId, value, WindowBlind.SINGLE, null);
		return new GenericStatus(true);
	}

	@ConsumeEvent(value = "WindowBlindDimMessage", blocking = true)
	public void callDim(WindowBlindDimMessage message) {
		setDim(message.getWindowBlindId(), message.getValue(), message.getType(), message.getRoomId());
	}

	@ConsumeEvent(value = "WindowBlindDimMessageSimple", blocking = true)
	public void setDim(WindowBlindDimMessageSimple event) {
		setDim(event.getWindowBlindId(), event.getValue());
	}

	@GET
	@Path("setDim/{windowBlind}/{value}/{type}/{roomId}")
	public GenericStatus setDim(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value,
			@PathParam("type") String type, @PathParam("roomId") Long roomId) {

		final Runnable windowBlindThread = () -> {

			String newValue = value;

			if (WindowBlind.SINGLE.equals(type)) {

				final WindowBlind singleWindowBlind1 = windowBlindMap.get(windowBlindId);

				if (Float.parseFloat(value) > singleWindowBlind1.getMaximumValue()) {
					newValue = Integer.toString(singleWindowBlind1.getMaximumValue());
				}

				String mqttDimTopic = singleWindowBlind1.getMqttDimTopic();
				if (mqttDimTopic != null && !mqttDimTopic.isEmpty()) {
					String dimMessage = singleWindowBlind1.getMqttDimMessage().replace(DIMVALUE, newValue);

					mqttSender.sendMQTTMessage(singleWindowBlind1.getMqttDimTopic(), dimMessage);
				} else {

					final String dimUrl1 = singleWindowBlind1.getDimUrl().replace(DIMVALUE, newValue);

					HTTPHelper.performHTTPRequest(dimUrl1);
					singleWindowBlind1.setCurrentValue(Float.parseFloat(newValue));

					em.getTransaction().begin();
					em.merge(singleWindowBlind1);
					em.getTransaction().commit();
				}

				final WindowBlindStatus eventData1 = new WindowBlindStatus();
				eventData1.setWindowBlind(singleWindowBlind1);
				final EventObject eventObject1 = new EventObject(eventData1);
				bus.publish("EventObject", eventObject1);
			} else if (WindowBlind.ALL_AT_ONCE.equals(type)) {
				final List<WindowBlind> windowBlinds = windowBlindList.get(roomId);

				for (final WindowBlind singleWindowBlind2 : windowBlinds) {

					String mqttDimTopic = singleWindowBlind2.getMqttDimTopic();
					if (mqttDimTopic != null && !mqttDimTopic.isEmpty()) {
						String dimMessage = singleWindowBlind2.getMqttDimMessage().replace(DIMVALUE, newValue);

						mqttSender.sendMQTTMessage(singleWindowBlind2.getMqttDimTopic(), dimMessage);
					} else {

						final String dimUrl1 = singleWindowBlind2.getDimUrl().replace(DIMVALUE, newValue);

						HTTPHelper.performHTTPRequest(dimUrl1);

						singleWindowBlind2.setCurrentValue(Float.parseFloat(newValue));

						em.getTransaction().begin();
						em.merge(singleWindowBlind2);
						em.getTransaction().commit();
					}

					final WindowBlindStatus eventData2 = new WindowBlindStatus();
					eventData2.setWindowBlind(singleWindowBlind2);
					final EventObject eventObject2 = new EventObject(eventData2);
					bus.publish("EventObject", eventObject2);

				}
			}

		};
		new Thread(windowBlindThread).start();

		return new GenericStatus(true);
	}

	@GET
	@Path("setPosition/{windowBlind}/{value}")
	public GenericStatus setPosition(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value) {

		em.getTransaction().begin();

		final WindowBlind singleWindowBlind = windowBlindMap.get(windowBlindId);

		singleWindowBlind.setCurrentValue(Float.parseFloat(value));
		em.merge(singleWindowBlind);
		em.getTransaction().commit();

		return new GenericStatus(true);
	}

	@ConsumeEvent(value = "WindowBlindPosition", blocking = true)
	public void setPositionEvent(WindowBlindPositionEvent event) {
		setPosition(event.getWindowBlindId(), event.getPosition());
	}

	public void dim(DimDirection dimDirection, long externalId) {
		final WindowBlind singleWindowBlind = windowBlindMap.get(externalId);

		if (singleWindowBlind != null) {

			float newPosition;
			float currentValue = singleWindowBlind.getCurrentValue();

			if (dimDirection.equals(DimDirection.UP)) {
				newPosition = currentValue + 10;
			} else {
				newPosition = currentValue - 10;
			}

			if (newPosition >= 0 && newPosition <= 100) {
				setDim(singleWindowBlind.getId(), Float.toString(newPosition));
			}
		}

	}

	@Scheduled(every = "120s")
	public void initWindowBlindList() {
		final

		List<Room> rooms = em.createQuery("select r from Room r", Room.class).getResultList();

		List<WindowBlind> windowBlindFullList = em.createQuery("select b from WindowBlind b", WindowBlind.class)
				.getResultList();
		Map<Long, WindowBlind> windowBlindMapTemp = new HashMap<>();

		for (WindowBlind windowBlind : windowBlindFullList) {
			windowBlindMapTemp.put(windowBlind.getId(), windowBlind);
		}

		Map<Long, List<WindowBlind>> windowBlindListTemp = new HashMap<>();
		if (rooms != null && !rooms.isEmpty()) {

			for (Room room : rooms) {

				Long roomId = room.getId();
				final List<WindowBlind> windowBlindRoomList = em
						.createQuery("select b from WindowBlind b where b.room=(select r from Room r where r.id=:room)",
								WindowBlind.class)
						.setParameter("room", roomId).getResultList();

				windowBlindListTemp.put(roomId, windowBlindRoomList);

			}

		}

		// replace once done
		windowBlindMap = windowBlindMapTemp;
		windowBlindList = windowBlindListTemp;
	}

}
