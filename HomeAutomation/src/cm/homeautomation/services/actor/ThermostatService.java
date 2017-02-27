package cm.homeautomation.services.actor;

import java.io.IOException;

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

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("thermostat")
public class ThermostatService extends BaseService {

	private void performHTTPSetPoint(String value, Switch singleSwitch, String setURL) {
		try {
			HttpGet getMethod = new HttpGet(setURL);
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();
			
			
			String[] userPassword = setURL.split("@")[0].replace("http://", "").split(":");
			
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
		        credsProvider.setCredentials(
		                new AuthScope(getMethod.getURI().getHost(), getMethod.getURI().getPort()),
		                new UsernamePasswordCredentials(userPassword[0], userPassword[1]));
		    clientBuilder.setDefaultCredentialsProvider(credsProvider);   
			HttpClient httpClient = clientBuilder.build();

			



			httpClient.execute(getMethod);
			
			
			singleSwitch.setLatestStatus(value);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@GET
	@Path("setValue/{id}/{value}")
	public GenericStatus setValue(@PathParam("id") Long id, @PathParam("value") String value) {

		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		Switch singleSwitch = (Switch)em.createQuery("select s from Switch s where s.id=:id").setParameter("id", id).getSingleResult();
		
		String setURL=singleSwitch.getSwitchSetUrl().replace("{SETVALUE}", value);
		
		performHTTPSetPoint(value, singleSwitch, setURL);
		
		em.persist(singleSwitch);
		
		em.getTransaction().commit();
		
		//TODO bind FHEM
		System.out.println("Set "+id+" to value: "+value);
		em.close();
		return new GenericStatus();
	}

}
