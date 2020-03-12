package cm.homeautomation.services.scheduler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.SchedulerThread;

@Path("scheduler/")
public class SchedulerService extends BaseService {
	
	@GET
	@Path("refresh")
	public SchedulerResponse refreshScheduler() {
		
		SchedulerResponse schedulerResponse = new SchedulerResponse();
		
		SchedulerThread.getInstance().reloadScheduler();
		
		schedulerResponse.setSuccess(true);
		return schedulerResponse;
	}
	
}
