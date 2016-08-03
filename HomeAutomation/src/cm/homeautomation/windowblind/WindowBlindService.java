package cm.homeautomation.windowblind;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpAuthenticator;
import org.apache.http.impl.client.HttpClientBuilder;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.services.base.BaseService;

@Path("windowBlinds")
public class WindowBlindService extends BaseService {

	public WindowBlindsList getAll() {
		WindowBlindsList windowBlindsList = new WindowBlindsList();

		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		@SuppressWarnings("unchecked")
		List<WindowBlind> windowBlinds = em.createQuery("select w FROM WindowBlind w ").getResultList();

		if (windowBlinds != null) {
			for (WindowBlind windowBlind : windowBlinds) {
				// windowBlind.setDimUrl(null);
				// windowBlind.setStatusUrl(null);
				windowBlindsList.getWindowBlinds().add(windowBlind);
			}
		}

		em.getTransaction().commit();
		return windowBlindsList;
	}

	@GET
	@Path("forRoom/{roomId}")
	public WindowBlindsList getAllForRoom(@PathParam("roomId") Long roomId) {
		WindowBlindsList windowBlindsList = new WindowBlindsList();

		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		@SuppressWarnings("unchecked")
		List<WindowBlind> windowBlinds = em
				.createQuery("select w FROM WindowBlind w where w.room=(select r from Room r where r.id=:roomId)")
				.setParameter("roomId", roomId).getResultList();

		if (windowBlinds != null && !windowBlinds.isEmpty()) {
			windowBlindsList.getWindowBlinds().addAll(windowBlinds);

			// create and entry for all at once
			WindowBlind allAtOnce = new WindowBlind();
			allAtOnce.setType(WindowBlind.ALL_AT_ONCE);
			allAtOnce.setName("Alle");
			allAtOnce.setCurrentValue(windowBlindsList.getWindowBlinds().get(0).getCurrentValue());
			allAtOnce.setRoom(windowBlindsList.getWindowBlinds().get(0).getRoom());
			windowBlindsList.getWindowBlinds().add(allAtOnce);

		}

		em.getTransaction().commit();
		return windowBlindsList;
	}

	@GET
	@Path("setDim/{windowBlind}/{value}")
	public void setDim(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value) {
		setDim(windowBlindId, value, WindowBlind.SINGLE, null);
	}

	@GET
	@Path("setDim/{windowBlind}/{value}/{type}/{roomId}")
	public void setDim(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value,
			@PathParam("type") String type, @PathParam("roomId") Long roomId) {
		EntityManager em = EntityManagerService.getNewManager();

		if (WindowBlind.SINGLE.equals(type)) {

			WindowBlind singleWindowBlind = (WindowBlind) em.createQuery("select w from WindowBlind w where w.id=:id")
					.setParameter("id", windowBlindId).getSingleResult();
			String dimUrl = singleWindowBlind.getDimUrl().replace("{DIMVALUE}", value);

			performHTTPRequest(dimUrl);

			singleWindowBlind.setCurrentValue(Float.parseFloat(value));

			em.getTransaction().begin();
			em.merge(singleWindowBlind);
			em.getTransaction().commit();
		} else if (WindowBlind.ALL_AT_ONCE.equals(type)) {

			@SuppressWarnings("unchecked")
			List<WindowBlind> windowBlinds = em
					.createQuery("select w FROM WindowBlind w where w.room=(select r from Room r where r.id=:roomId)")
					.setParameter("roomId", roomId).getResultList();

			for (WindowBlind singleWindowBlind : windowBlinds) {
				String dimUrl = singleWindowBlind.getDimUrl().replace("{DIMVALUE}", value);

				performHTTPRequest(dimUrl);

				singleWindowBlind.setCurrentValue(Float.parseFloat(value));

				em.getTransaction().begin();
				em.merge(singleWindowBlind);
				em.getTransaction().commit();
			}
		}
	}

	@GET
	@Path("setPosition/{windowBlind}/{value}")
	public void setPosition(@PathParam("windowBlind") Long windowBlindId, @PathParam("value") String value) {
		EntityManager em = EntityManagerService.getNewManager();

		em.getTransaction().begin();

		WindowBlind singleWindowBlind = (WindowBlind) em.createQuery("select w from WindowBlind w where w.id=:id")
				.setParameter("id", windowBlindId).getSingleResult();

		singleWindowBlind.setCurrentValue(Float.parseFloat(value));
		em.merge(singleWindowBlind);
		em.getTransaction().commit();
	}

	private void performHTTPRequest(String url) {
		try {
			HttpGet getMethod = new HttpGet(url);
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			String[] userPassword = url.split("@")[0].replace("http://", "").split(":");

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(getMethod.getURI().getHost(), getMethod.getURI().getPort()),
					new UsernamePasswordCredentials(userPassword[0], userPassword[1]));
			clientBuilder.setDefaultCredentialsProvider(credsProvider);
			HttpClient httpClient = clientBuilder.build();

			httpClient.execute(getMethod);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * call this method to set a specific dim value
	 * @param args
	 */
	public synchronized static void cronSetDim(String[] args) {
		String windowBlindId = args[0];
		String dimValue = args[1];

		new WindowBlindService().setDim(new Long(windowBlindId), dimValue);
	}

	/**
	 * call this method to perform a calibration of the window blind
	 * 
	 * @param args
	 */
	public synchronized static void cronPerformCalibration(String[] args) {
		String windowBlindId = args[0];

		new WindowBlindService().performCalibration(new Long(windowBlindId));
	}

	public void performCalibration(Long windowBlindId) {
		EntityManager em = EntityManagerService.getNewManager();
		WindowBlind windowBlind = em.find(WindowBlind.class, windowBlindId);

		if (windowBlind != null) {
			String calibrationUrl = windowBlind.getCalibrationUrl();

			if (calibrationUrl != null && !("".equals(calibrationUrl))) {
				performHTTPRequest(calibrationUrl);
			}
		}

	}

}
