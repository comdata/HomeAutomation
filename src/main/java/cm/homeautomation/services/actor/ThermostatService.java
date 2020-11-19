package cm.homeautomation.services.actor;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
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

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("thermostat/")
public class ThermostatService extends BaseService {

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	private static ThermostatService instance = null;

	public static void cronSetStatus(final String[] args) {
		final Long id = Long.valueOf(args[0]);
		final String value = args[1];
		getInstance().setValue(id, value);
	}

	public static ThermostatService getInstance() {

		if (instance == null) {
			instance = new ThermostatService();
		}

		return instance;
	}

	public ThermostatService() {
		instance = this;
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
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

	@GET
	@Path("setValue/{id}/{value}")
	@Transactional
	public GenericStatus setValue(@PathParam("id") final Long id, @PathParam("value") String value) {

		final Switch singleSwitch = em.createQuery("select s from Switch s where s.id=:id", Switch.class)
				.setParameter("id", id).getSingleResult();

		final String setURL = singleSwitch.getSwitchSetUrl().replace("{SETVALUE}", value);

		performHTTPSetPoint(value, singleSwitch, setURL);

		singleSwitch.setLatestStatus(value);
		singleSwitch.setLatestStatusFrom(new Date());

		em.persist(singleSwitch);

		value = value.replaceAll("[\n|\r|\t]", "_");

		LogManager.getLogger(this.getClass()).info("Set {} to value: {}", id, value);
		return new GenericStatus(true);
	}

}
