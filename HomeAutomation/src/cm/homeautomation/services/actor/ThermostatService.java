package cm.homeautomation.services.actor;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("thermostat")
public class ThermostatService extends BaseService {

	private void performHTTPSetPoint(String value, Switch singleSwitch, String setURL) {
		try {
			GetMethod getMethod = new GetMethod(setURL);
			HttpClient httpClient = new HttpClient();

			String[] userPassword=setURL.split("@")[0].replace("http://", "").split(":");
			
			
			
			Credentials defaultcreds = new UsernamePasswordCredentials(userPassword[0], userPassword[1]);
			System.out.println("URL: "+setURL);
			System.out.println(getMethod.getURI().getUserinfo());
			httpClient.getState().setCredentials(new AuthScope(getMethod.getURI().getHost(), getMethod.getURI().getPort(), AuthScope.ANY_REALM),
					defaultcreds);

			httpClient.executeMethod(getMethod);
			
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
		
		return new GenericStatus();
	}

}
