package cm.homeautomation.services.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.SchedulerThread;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskTable;

@Path("scheduler")
public class SchedulerService extends BaseService {

	@GET
	@Path("refresh")
	public SchedulerResponse refreshScheduler() {

		SchedulerResponse schedulerResponse = new SchedulerResponse();

		SchedulerThread.getInstance().reloadScheduler();

		schedulerResponse.setSuccess(true);
		return schedulerResponse;
	}


	@GET
	@Path("getTasks")
	public List<Task> getSchedulerEntries() {

		List<Task> taskList = new ArrayList<>();

		TaskTable tasks = SchedulerThread.getInstance().getScheduler().getTaskCollectors()[0].getTasks();

		File[] scheduledFiles = SchedulerThread.getInstance().getScheduler().getScheduledFiles();

		for (int i = 0; i < tasks.size(); i++) {

			Task task = tasks.getTask(i);

			taskList.add(task);
		}

		return taskList;
	}

}
