package cm.homeautomation.services.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Task;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.SchedulerThread;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
@Path("scheduler")
public class SchedulerService extends BaseService {

	@Inject
	org.quartz.Scheduler quartz;

	public SchedulerService() {

	}

	void onStart(@Observes StartupEvent event) {
		try {
			initialize();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initialize() throws SchedulerException {

		EntityManager em = EntityManagerService.getManager();

		List<Task> tasks = em.createQuery("select t from Task t", Task.class)
				.setHint("javax.persistence.cache.storeMode", "REFRESH").getResultList();

		if (tasks != null && !tasks.isEmpty()) {

			for (Task task : tasks) {

				try {
					JobDataMap newJobDataMap = new JobDataMap();
					newJobDataMap.put("clazz", task.getClazz());
					newJobDataMap.put("method", task.getMethod());
					newJobDataMap.put("arguments", task.getArguments());
					String group = task.getClazz() + "." + task.getMethod();
					String name = "ID_" + task.getId();
					JobDetail job = JobBuilder.newJob(SingleJobClass.class).usingJobData(newJobDataMap)
							.withIdentity(name, group).build();

					String cronExpression = task.getCronExpression();

					CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
							.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

					JobKey jobKey = new JobKey(name, group);
					JobDetail existingJob = quartz.getJobDetail(jobKey);

					if (existingJob != null) {
						quartz.deleteJob(jobKey);
					}

					quartz.scheduleJob(job, trigger);
				} catch (SchedulerException e) {
					e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SchedulerResponse schedulerResponse = new SchedulerResponse();

		SchedulerThread.getInstance().reloadScheduler();

		schedulerResponse.setSuccess(true);
		return schedulerResponse;
	}

	@GET
	@Path("getTasks")
	public List<Task> getSchedulerEntries() {

		return null;
	}

	@GET
	@Path("sampleTask")
	public void createSampleTask() {
		EntityManager em = EntityManagerService.getManager();

		Task task = new Task();
		List<String> arguments = new ArrayList<>();
		arguments.add("Argument 1");
		arguments.add("Argument 2");
		task.setArguments(arguments);

		em.getTransaction().begin();
		em.persist(task);

		em.getTransaction().commit();
	}

	@GET
	@Path("import")
	public void importScheduleFile() {

		EntityManager em = EntityManagerService.getManager();

		File cronFile = new File("/Users/christoph/git/HomeAutomation/schedule.cron");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(cronFile));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				// read next line
				line = reader.readLine();

				if (line != null) {
					if (line.startsWith("#")) {
						continue;
					} else if ("".equals(line.trim())) {
						continue;
					} else {
						// System.out.println(line);

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

						em.getTransaction().begin();

						em.persist(task);

						em.getTransaction().commit();

					}
				}

			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
