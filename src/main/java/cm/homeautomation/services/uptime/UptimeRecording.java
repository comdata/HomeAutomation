package cm.homeautomation.services.uptime;

import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.UptimePing;
import cm.homeautomation.services.scheduler.JobArguments;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;

@Startup
public class UptimeRecording {

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;



	@ConsumeEvent(value = "UptimeRecording", blocking = true)

	public void recordUptime(JobArguments args) {

		UptimePing uptimePing = new UptimePing();
		uptimePing.setTimestamp(new Date());
		uptimePing.setUp(true);

		em.persist(uptimePing);

	}

}
