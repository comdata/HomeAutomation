package cm.homeautomation.services.scheduler;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@ApplicationScoped
public class SingleJobClass implements Job {

	public SingleJobClass() {
		super();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

//		try {
			String clazz = (String) context.getJobDetail().getJobDataMap().get("clazz");

			String method = (String) context.getJobDetail().getJobDataMap().get("method");
			Object object = context.getJobDetail().getJobDataMap().get("arguments");

			if (object instanceof List) {

				List<?> argumentList = (List<?>) object;

				String[] arguments = (argumentList != null) ? argumentList.toArray(new String[0]) : new String[0];

//				Method specificMethod = Class.forName(clazz).getMethod(method, String[].class);
//
//				if (specificMethod != null) {
////                System.out.println(
////                        "Invoking method Task called. " + clazz + "." + method + " arguments: " + argumentString);
//
//					final Object[] args = new Object[1];
//					args[0] = arguments;
//
//					specificMethod.invoke(null, args);
//				}
			}
//		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalArgumentException
//				| IllegalAccessException | InvocationTargetException e) {
//			LogManager.getLogger(this.getClass()).error(e);
//		}

	}

}