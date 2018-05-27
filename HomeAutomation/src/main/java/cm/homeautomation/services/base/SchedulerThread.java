package cm.homeautomation.services.base;

import java.io.File;

import javax.security.auth.login.Configuration;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import it.sauronsoftware.cron4j.Scheduler;

/**
 * provide refresh methods for the scheduler
 * 
 * @author mertins
 *
 */
@AutoCreateInstance
public class SchedulerThread {
	private static SchedulerThread instance = null;
	private Scheduler scheduler;
	private boolean run = true;
	private File scheduleFile;
	
	public SchedulerThread() {
		instance = this;
		instance.reloadScheduler();
	}

	public static SchedulerThread getInstance() {
		if (instance == null) {
			instance = new SchedulerThread();
			instance.reloadScheduler();
		}

		return instance;
	}

	public void reloadScheduler() {
		Scheduler scheduler = getScheduler();
		if (scheduler != null && scheduleFile != null) {
			scheduler.descheduleFile(scheduleFile);

		}

		String configFile = ConfigurationService.getConfigurationProperty("scheduler", "configFile");
		
		scheduleFile = new File(configFile);
		scheduler.scheduleFile(scheduleFile);

		if (!scheduler.isStarted()) {
			scheduler.start();
		}

		LogManager.getLogger(this.getClass()).info("Reloaded scheduler");
	}
	
	public void stopScheduler() {
		getScheduler().stop();
	}

	public Scheduler getScheduler() {
		if (scheduler == null) {
			scheduler = new Scheduler();
		}
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}
