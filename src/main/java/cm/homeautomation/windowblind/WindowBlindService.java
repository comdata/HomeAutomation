package cm.homeautomation.windowblind;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.base.HTTPHelper;

@Path("windowBlinds")
public class WindowBlindService extends BaseService {

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

		final EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		@SuppressWarnings("unchecked")
		final List<WindowBlind> windowBlinds = em.createQuery("select w FROM WindowBlind w ").getResultList();

		if (windowBlinds != null) {
			for (final WindowBlind windowBlind : windowBlinds) {
				windowBlindsList.getWindowBlinds().add(windowBlind);
			}
		}

		em.getTransaction().commit();
		em.close();
		return windowBlindsList;
	}

	@GET
	@Path("forRoom/{roomId}")
	public WindowBlindsList getAllForRoom(@PathParam("roomId") Long roomId) {
		final WindowBlindsList windowBlindsList = new WindowBlindsList();

		final EntityManager em = EntityManagerService.getNewManager();
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

		em.close();
		return windowBlindsList;
	}

	public void performCalibration(Long windowBlindId) {
		final EntityManager em = EntityManagerService.getNewManager();
		final WindowBlind windowBlind = em.find(WindowBlind.class, windowBlindId);

		if (windowBlind != null) {
			final String calibrationUrl = windowBlind.getCalibrationUrl();

			if ((calibrationUrl != null) && !("".equals(calibrationUrl))) {
				HTTPHelper.performHTTPRequest(calibrationUrl);
			}
		}
		em.close();

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
		final EntityManager em = EntityManagerService.getNewManager();

		if (Float.parseFloat(value) > 99) {
			value = "99";
		}

		final String newValue = value;

		final Runnable windowBlindThread = () -> {

			if (WindowBlind.SINGLE.equals(type)) {

				final WindowBlind singleWindowBlind1 = (WindowBlind) em
						.createQuery("select w from WindowBlind w where w.id=:id").setParameter("id", windowBlindId)
						.getSingleResult();
				final String dimUrl1 = singleWindowBlind1.getDimUrl().replace("{DIMVALUE}", newValue);

				HTTPHelper.performHTTPRequest(dimUrl1);

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
					final String dimUrl2 = singleWindowBlind2.getDimUrl().replace("{DIMVALUE}", newValue);

					HTTPHelper.performHTTPRequest(dimUrl2);

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
			em.close();
		};
		new Thread(windowBlindThread).start();

		return new GenericStatus(true);
	}

	@GET
	@Path("setPosition/{windowBlind}/{value}")
	public GenericStatus setPosition(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value) {
		final EntityManager em = EntityManagerService.getNewManager();

		em.getTransaction().begin();

		final WindowBlind singleWindowBlind = (WindowBlind) em.createQuery("select w from WindowBlind w where w.id=:id")
				.setParameter("id", windowBlindId).getSingleResult();

		singleWindowBlind.setCurrentValue(Float.parseFloat(value));
		em.merge(singleWindowBlind);
		em.getTransaction().commit();
		em.close();
		
		return new GenericStatus(true);
	}

}
