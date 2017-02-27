package cm.homeautomation.services.uptime;

import java.util.Date;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.UptimePing;

public class UptimeRecording {

	public static void recordUptime(String[] args) {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		UptimePing uptimePing=new UptimePing();
		uptimePing.setTimestamp(new Date());
		uptimePing.setUp(true);
		
		em.persist(uptimePing);
		
		em.getTransaction().commit();
	}
	
}
