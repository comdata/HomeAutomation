package cm.homeautomation.services.uptime;

import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.UptimePing;
import io.quarkus.runtime.Startup;

@Startup
public class UptimeRecording {

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	private static UptimeRecording instance;
	
	public UptimeRecording() {
		instance=this;
	}
	
	public static void recordUptime(String[] args) {
		instance.internalRecordUptime(args);
	}
	
	public void internalRecordUptime(String[] args) {
		
		
		em.getTransaction().begin();
		
		UptimePing uptimePing=new UptimePing();
		uptimePing.setTimestamp(new Date());
		uptimePing.setUp(true);
		
		em.persist(uptimePing);
		
		em.getTransaction().commit();
	}
	
}
