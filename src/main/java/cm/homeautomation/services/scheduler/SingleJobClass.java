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
		System.out.println("clazz: "+clazz);
		String[] clazzParts = clazz.split("\\.");
		System.out.println(clazzParts.length);
		String shortClazz = clazzParts[clazzParts.length-1];
		List<String> argumentList = (List<String>) context.getJobDetail().getJobDataMap().get("arguments");

		JobArguments jobArguments = new JobArguments(argumentList);
		
		System.out.println(argumentList);

		for (String object2 : argumentList) {
			System.out.println(object2);
		}

		String[] arguments = argumentList.toArray(new String[0]);
		System.out.println(arguments);

		bus.publish(shortClazz, jobArguments);

	}

}