package cm.homeautomation.services.scheduler;

import org.apache.log4j.LogManager;

public class DummyTask {

	public static void execute(String[] args) {
		LogManager.getLogger(DummyTask.class).debug("Dummy Task called");
	}
}