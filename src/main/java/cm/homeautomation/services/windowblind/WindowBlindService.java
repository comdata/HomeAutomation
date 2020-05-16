package cm.homeautomation.services.windowblind;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.base.HTTPHelper;

@Path("windowBlinds/")
public class WindowBlindService extends BaseService {

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

	public WindowBlindsList getAll() {
		final WindowBlindsList windowBlindsList = new WindowBlindsList();

		final EntityManager em = EntityManagerService.getManager();
		em.getTransaction().begin();

		@SuppressWarnings("unchecked")
		final List<WindowBlind> windowBlinds = em.createQuery("select w FROM WindowBlind w ").getResultList();

		if (windowBlinds != null) {
			for (final WindowBlind windowBlind : windowBlinds) {
				windowBlindsList.getWindowBlinds().add(windowBlind);
			}
		}

		em.getTransaction().commit();

		return windowBlindsList;
	}

	@GET
	@Path("forRoom/{roomId}")
	public WindowBlindsList getAllForRoom(@PathParam("roomId") Long roomId) {
		final WindowBlindsList windowBlindsList = new WindowBlindsList();

		final EntityManager em = EntityManagerService.getManager();
		em.getTransaction().begin();

		@SuppressWarnings("unchecked")
		final List<WindowBlind> windowBlinds = em
				.createQuery("select w FROM WindowBlind w where w.room=(select r from Room r where r.id=:roomId)")
				.setParameter("roomId", roomId).getResultList();

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
		final EntityManager em = EntityManagerService.getManager();
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

	@GET
	@Path("setDim/{windowBlind}/{value}/{type}/{roomId}")
	public GenericStatus setDim(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value,
			@PathParam("type") String type, @PathParam("roomId") Long roomId) {
		final EntityManager em = EntityManagerService.getManager();

		if (Float.parseFloat(value) > 99) {
			value = "99";
		}

		final String newValue = value;

		final Runnable windowBlindThread = () -> {

			if (WindowBlind.SINGLE.equals(type)) {

				final WindowBlind singleWindowBlind1 = (WindowBlind) em
						.createQuery("select w from WindowBlind w where w.id=:id").setParameter("id", windowBlindId)
						.getSingleResult();

				String mqttDimTopic = singleWindowBlind1.getMqttDimTopic();
				if (mqttDimTopic != null && !mqttDimTopic.isEmpty()) {
					String dimMessage = singleWindowBlind1.getMqttDimMessage().replace("{DIMVALUE}", newValue);

					MQTTSender.sendMQTTMessage(singleWindowBlind1.getMqttDimTopic(), dimMessage);
				} else {

					final String dimUrl1 = singleWindowBlind1.getDimUrl().replace("{DIMVALUE}", newValue);

					HTTPHelper.performHTTPRequest(dimUrl1);
				}

				singleWindowBlind1.setCurrentValue(Float.parseFloat(newValue));

				em.getTransaction().begin();
				em.merge(singleWindowBlind1);
				em.getTransaction().commit();

				final WindowBlindStatus eventData1 = new WindowBlindStatus();
				eventData1.setWindowBlind(singleWindowBlind1);
				final EventObject eventObject1 = new EventObject(eventData1);
				EventBusService.getEventBus().post(eventObject1);
			} else if (WindowBlind.ALL_AT_ONCE.equals(type)) {
				@SuppressWarnings("unchecked")
				final List<WindowBlind> windowBlinds = em
						.createQuery(
								"select w FROM WindowBlind w where w.room=(select r from Room r where r.id=:roomId)")
						.setParameter("roomId", roomId).getResultList();

				for (final WindowBlind singleWindowBlind2 : windowBlinds) {

					String mqttDimTopic = singleWindowBlind2.getMqttDimTopic();
					if (mqttDimTopic != null && !mqttDimTopic.isEmpty()) {
						String dimMessage = singleWindowBlind2.getMqttDimMessage().replace("{DIMVALUE}", newValue);

						MQTTSender.sendMQTTMessage(singleWindowBlind2.getMqttDimTopic(), dimMessage);
					} else {

						final String dimUrl1 = singleWindowBlind2.getDimUrl().replace("{DIMVALUE}", newValue);

						HTTPHelper.performHTTPRequest(dimUrl1);
					}

					singleWindowBlind2.setCurrentValue(Float.parseFloat(newValue));

					em.getTransaction().begin();
					em.merge(singleWindowBlind2);
					em.getTransaction().commit();

					final WindowBlindStatus eventData2 = new WindowBlindStatus();
					eventData2.setWindowBlind(singleWindowBlind2);
					final EventObject eventObject2 = new EventObject(eventData2);
					EventBusService.getEventBus().post(eventObject2);

				}
			}

		};
		new Thread(windowBlindThread).start();

		return new GenericStatus(true);
	}

	@GET
	@Path("setPosition/{windowBlind}/{value}")
	public GenericStatus setPosition(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value) {
		final EntityManager em = EntityManagerService.getManager();

		em.getTransaction().begin();

		final WindowBlind singleWindowBlind = (WindowBlind) em.createQuery("select w from WindowBlind w where w.id=:id")
				.setParameter("id", windowBlindId).getSingleResult();

		singleWindowBlind.setCurrentValue(Float.parseFloat(value));
		em.merge(singleWindowBlind);
		em.getTransaction().commit();

		return new GenericStatus(true);
	}

	public void dim(DimDirection dimDirection, long externalId) {
		final EntityManager em = EntityManagerService.getManager();

		final WindowBlind singleWindowBlind = (WindowBlind) em
				.createQuery("select w from WindowBlind w where w.externalId=:externalId")
				.setParameter("externalId", Long.toString(externalId)).getSingleResult();

		if (singleWindowBlind != null) {

			float newPosition;
			float currentValue = singleWindowBlind.getCurrentValue();

			if (dimDirection.equals(DimDirection.UP)) {
				newPosition = currentValue + 10;
			} else {
				newPosition = currentValue - 10;
			}

			if (newPosition > 0 && newPosition < 100) {
				setDim(singleWindowBlind.getId(), Float.toString(newPosition));
			}
		}

	}

}
