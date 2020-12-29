package cm.homeautomation.services.scheduler;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class SingleJobClass implements Job {

	@Inject
	EventBus bus;

	public SingleJobClass() {
		super();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		String clazz = (String) context.getJobDetail().getJobDataMap().get("clazz");
		String[] clazzParts = clazz.split("\\.");
		String shortClazz = clazzParts[clazzParts.length - 1];
		@SuppressWarnings("unchecked")
		List<String> argumentList = (List<String>) context.getJobDetail().getJobDataMap().get("arguments");

		JobArguments jobArguments = new JobArguments(argumentList);

		bus.publish(shortClazz, jobArguments);

	}

}