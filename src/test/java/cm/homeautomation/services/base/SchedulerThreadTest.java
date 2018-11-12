package cm.homeautomation.services.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import cm.homeautomation.configuration.ConfigurationService;
import it.sauronsoftware.cron4j.Scheduler;

public class SchedulerThreadTest {

	@BeforeEach
	public void setup() {
		ConfigurationService.createOrUpdate("scheduler", "configFile", "schedule.cron");
	}
	
	@Test
	public void testGetInstance() throws Exception {
		SchedulerThread instance = SchedulerThread.getInstance();
		assertNotNull(instance);
	}

	@Test
	public void testReloadScheduler() throws Exception {
		SchedulerThread instance = SchedulerThread.getInstance();
		boolean reloadedScheduler = instance.reloadScheduler();

		assertTrue(reloadedScheduler);
	}

	@Test
	public void testStopScheduler() throws Exception {
		SchedulerThread instance = SchedulerThread.getInstance();
		instance.getScheduler().start();
		instance.stopScheduler();

		assertFalse(instance.getScheduler().isStarted());
	}

	@Test
	public void testGetScheduler() throws Exception {
		SchedulerThread instance = SchedulerThread.getInstance();
		assertNotNull(instance.getScheduler());
	}

	@Test
	public void testSetScheduler() throws Exception {
		SchedulerThread instance = SchedulerThread.getInstance();
		Scheduler newScheduler = new Scheduler();
		
		assertNotEquals(newScheduler, instance.getScheduler());	
		instance.setScheduler( newScheduler);
		assertEquals(newScheduler, instance.getScheduler());
	}

}
