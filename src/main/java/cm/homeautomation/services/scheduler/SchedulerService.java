package cm.homeautomation.services.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.LogManager;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Task;
import cm.homeautomation.services.base.BaseService;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
@Path("scheduler")
public class SchedulerService extends BaseService {

	Map<Long, JobKey> jobMap = new HashedMap<>();

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Inject
	org.quartz.Scheduler quartz;

	public SchedulerService() {

	}

	void onStart(@Observes StartupEvent event) {
		try {
			initialize();
		} catch (SchedulerException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

	private void initialize() throws SchedulerException {

		List<Task> tasks = em.createQuery("select t from Task t", Task.class)
				.setHint("javax.persistence.cache.storeMode", "REFRESH").getResultList();

		if (tasks != null && !tasks.isEmpty()) {

			for (Task task : tasks) {

				try {
					String group = task.getClazz() + "." + task.getMethod();
					Long taskId = task.getId();
					String name = "ID_" + taskId;

					JobKey jobKey = new JobKey(name, group);
					JobDetail existingJob = quartz.getJobDetail(jobKey);

					if (existingJob != null) {
						quartz.deleteJob(jobKey);
						jobMap.remove(taskId);
					}

					if (task.isEnabled()) {
						JobDataMap newJobDataMap = new JobDataMap();
						newJobDataMap.put("clazz", task.getClazz());
						newJobDataMap.put("method", task.getMethod());
						newJobDataMap.put("arguments", task.getArguments());

						JobDetail job = JobBuilder.newJob(SingleJobClass.class).usingJobData(newJobDataMap)
								.withIdentity(name, group).build();

						String cronExpression = task.getCronExpression();

						CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
								.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

						quartz.scheduleJob(job, trigger);
						jobMap.put(taskId, jobKey);
					}
				} catch (SchedulerException e) {
					LogManager.getLogger(this.getClass()).error(e);
				}
			}

		}

		if (!quartz.isStarted()) {
			quartz.start();
		}
	}

	@GET
	@Path("refresh")
	public SchedulerResponse refreshScheduler() {

		try {
			this.initialize();
		} catch (SchedulerException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

		SchedulerResponse schedulerResponse = new SchedulerResponse();

		schedulerResponse.setSuccess(true);
		return schedulerResponse;
	}

	@GET
	@Path("getTasks")
	public Map<Long, JobKey> getSchedulerEntries() {
		return jobMap;
	}

	@GET
	@Path("sampleTask")
	
	public void createSampleTask() {

		Task task = new Task();
		List<String> arguments = new ArrayList<>();
		arguments.add("Argument 1");
		arguments.add("Argument 2");
		task.setArguments(arguments);

		em.persist(task);

	}

	@GET
	@Path("import")
	
	public void importScheduleFile() {

		File cronFile = new File("/Users/christoph/git/HomeAutomation/schedule.cron");

		try (BufferedReader reader = new BufferedReader(new FileReader(cronFile));) {
			String line = reader.readLine();
			while (line != null) {
				// read next line
				line = reader.readLine();

				if (line != null) {
					if (line.startsWith("#")) {
						continue;
					} else if ("".equals(line.trim())) {
						continue;
					} else {

						String[] lineParts = line.split("\\s+");
						System.out.println("Line parts: " + lineParts.length);

						String cron = lineParts[0] + " " + lineParts[1] + " " + lineParts[2] + " " + lineParts[3] + " "
								+ lineParts[4] + " ?";
						String clazz = lineParts[5].split("#")[0].replace("java:", "");
						String method = lineParts[5].split("#")[1];
						List<String> arguments = new ArrayList<>();
						if (lineParts.length > 5) {
							for (int i = 6; i < lineParts.length; i++) {
								arguments.add(lineParts[i].replace("\"", ""));
							}
						}

						Task task = new Task();
						task.setCronExpression(cron);
						task.setMethod(method);
						task.setClazz(clazz);
						task.setArguments(arguments);

						em.persist(task);

					}
				}

			}
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

}
