package cm.homeautomation.services.actor;

import java.io.IOException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("thermostat")
public class ThermostatService extends BaseService {

	private static ThermostatService instance;

	public static ThermostatService getInstance() {

		if (instance == null) {
			new ThermostatService();
		}

		return instance;
	}

	public ThermostatService() {
		instance = this;
	}

	public void cronSetStatus(final String[] args) {
		final Long id = new Long(args[0]);
		final String value = args[1];
		getInstance().setValue(id, value);
	}

	private void performHTTPSetPoint(final String value, final Switch singleSwitch, final String setURL) {
		try {
			final HttpGet getMethod = new HttpGet(setURL);
			final HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			final String[] userPassword = setURL.split("@")[0].replace("http://", "").split(":");

			final CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(getMethod.getURI().getHost(), getMethod.getURI().getPort()),
					new UsernamePasswordCredentials(userPassword[0], userPassword[1]));
			clientBuilder.setDefaultCredentialsProvider(credsProvider);
			final HttpClient httpClient = clientBuilder.build();

			httpClient.execute(getMethod);

			singleSwitch.setLatestStatus(value);

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GET
	@Path("setValue/{id}/{value}")
	public GenericStatus setValue(@PathParam("id") final Long id, @PathParam("value") final String value) {

		final EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		final Switch singleSwitch = (Switch) em.createQuery("select s from Switch s where s.id=:id")
				.setParameter("id", id).getSingleResult();

		final String setURL = singleSwitch.getSwitchSetUrl().replace("{SETVALUE}", value);

		performHTTPSetPoint(value, singleSwitch, setURL);

		singleSwitch.setLatestStatus(value);
		singleSwitch.setLatestStatusFrom(new Date());

		em.persist(singleSwitch);

		em.getTransaction().commit();

		// TODO bind FHEM
		LogManager.getLogger(this.getClass()).info("Set " + id + " to value: " + value);
		em.close();
		return new GenericStatus(true);
	}

}
