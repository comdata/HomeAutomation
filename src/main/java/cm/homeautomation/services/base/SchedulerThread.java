package cm.homeautomation.services.base;

import java.io.File;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.TaskCollector;

/**
 * provide refresh methods for the scheduler
 * 
 * @author christoph
 *
 */
@AutoCreateInstance
public class SchedulerThread {
	private static SchedulerThread instance = null;
	private Scheduler scheduler;
	private File scheduleFile;

	public SchedulerThread() {
		SchedulerThread.setInstance(this);

		TaskCollector customTaskCollector = new CustomTaskCollector();
		this.getScheduler().addTaskCollector(customTaskCollector);
		this.reloadScheduler();
	}

	public static SchedulerThread getInstance() {
		if (instance == null) {
			instance = new SchedulerThread();
			instance.reloadScheduler();
		}

		return instance;
	}

	public boolean reloadScheduler() {
		Scheduler currentScheduler = getScheduler();

		if (scheduleFile != null) {
			currentScheduler.descheduleFile(scheduleFile);
		}

		String configFile = ConfigurationService.getConfigurationProperty("scheduler", "configFile");

		scheduleFile = new File(configFile);
		currentScheduler.scheduleFile(scheduleFile);

		if (!currentScheduler.isStarted()) {
			currentScheduler.start();
		}

		LogManager.getLogger(this.getClass()).info("Reloaded scheduler");
		return true;

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

	public static void setInstance(SchedulerThread instance) {
		SchedulerThread.instance = instance;
	}
}
