package cm.homeautomation.services.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.LogManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class SingleJobClass implements Job {

    public SingleJobClass() {
        super();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {
            String clazz = (String) context.getJobDetail().getJobDataMap().get("clazz");

            String method = (String) context.getJobDetail().getJobDataMap().get("method");
            Object object = context.getJobDetail().getJobDataMap().get("arguments");

            if (object instanceof List) {

                List<?> argumentList = (List<?>) object;

                String[] arguments = (argumentList != null) ? argumentList.toArray(new String[0]) : new String[0];

                Method specificMethod = Class.forName(clazz).getMethod(method, String[].class);

                String argumentString = "";
                for (String argument : arguments) {
                    argumentString += argument + "; ";
                }

                System.out.println(
                        "Invoking method Task called. " + clazz + "." + method + " arguments: " + argumentString);

                final Object[] args = new Object[1];
                args[0] = arguments;

                specificMethod.invoke(null, args);
            }
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalArgumentException
                | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        	LogManager.getLogger(this.getClass()).error(e);
        }

    }

}