package cm.homeautomation.windowblind;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

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

			performHTTPDim(value, singleWindowBlind, dimUrl);

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

				performHTTPDim(value, singleWindowBlind, dimUrl);

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

	private void performHTTPDim(String value, WindowBlind singleWindowBlind, String dimUrl) {
		try {
			GetMethod getMethod = new GetMethod(dimUrl);
			HttpClient httpClient = new HttpClient();

			String[] userPassword = dimUrl.split("@")[0].replace("http://", "").split(":");

			Credentials defaultcreds = new UsernamePasswordCredentials(userPassword[0], userPassword[1]);

			System.out.println(getMethod.getURI().getUserinfo());
			httpClient.getState().setCredentials(
					new AuthScope(getMethod.getURI().getHost(), getMethod.getURI().getPort(), AuthScope.ANY_REALM),
					defaultcreds);

			httpClient.executeMethod(getMethod);

			singleWindowBlind.setCurrentValue(new Long(value));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized static void cronSetDim(String[] args) {
		String windowBlindId = args[0];
		String dimValue = args[1];

		new WindowBlindService().setDim(new Long(windowBlindId), dimValue);
	}

}
