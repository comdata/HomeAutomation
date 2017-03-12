package cm.homeautomation.services.base;

import java.io.File;

import org.apache.log4j.Logger;

import it.sauronsoftware.cron4j.Scheduler;

/**
 * provide refresh methods for the scheduler
 * 
 * @author mertins
 *
 */
public class SchedulerThread {
	private static SchedulerThread instance = null;
	private Scheduler scheduler;
	private boolean run = true;
	private File scheduleFile;

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

		scheduleFile = new File("/var/lib/tomcat8/webapps/HomeAutomation/schedule.cron");
		scheduler.scheduleFile(scheduleFile);

		if (!scheduler.isStarted()) {
			scheduler.start();
		}

		Logger.getLogger(this.getClass()).info("Reloaded scheduler");
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
