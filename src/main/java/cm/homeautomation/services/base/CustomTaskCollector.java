package cm.homeautomation.services.base;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.SchedulingEntity;
import it.sauronsoftware.cron4j.CronParser;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

public class CustomTaskCollector implements TaskCollector {
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
				LogManager.getLogger(this.getClass()).error("evaluation failed. pattern: "
						+ schedulingEntity.getPattern() + " action: " + schedulingEntity.getTaskAction(), e);
			}
		}

		return taskTable;
	}
}