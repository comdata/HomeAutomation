package cm.homeautomation.services.base;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.SchedulingEntity;
import it.sauronsoftware.cron4j.CronParser;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

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

		TaskCollector customTaskCollector = new TaskCollector() {

			@Override
			public TaskTable getTasks() {
				TaskTable taskTable = new TaskTable();

				EntityManager em = EntityManagerService.getNewManager();

				List<SchedulingEntity> resultList = em
						.createQuery("select s from SchedulingEntity s", SchedulingEntity.class).getResultList();

				for (SchedulingEntity schedulingEntity : resultList) {

					try {
						CronParser.parseLine(taskTable,
								schedulingEntity.getPattern() + "\t" + schedulingEntity.getTaskAction());
					} catch (Exception e) {
						LogManager.getLogger(this.getClass()).error("evaluation failed. pattern: "+schedulingEntity.getPattern()+" action: "+schedulingEntity.getTaskAction(), e);
					}
				}

				return taskTable;
			}
		};
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

	public void reloadScheduler() {
		Scheduler currentScheduler = getScheduler();
		if (currentScheduler != null) {

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
		}
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
