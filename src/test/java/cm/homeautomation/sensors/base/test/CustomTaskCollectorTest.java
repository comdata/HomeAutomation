package cm.homeautomation.sensors.base.test;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.SchedulingEntity;
import cm.homeautomation.services.base.CustomTaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

class CustomTaskCollectorTest {

	@BeforeClass
	void setup() {
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		em.createNativeQuery("delete from SCHEDULINGENTITY").executeUpdate();
		em.getTransaction().commit();
		
	}
	
	@Test
	void testGetTask() {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		SchedulingEntity schedulingEntity = new SchedulingEntity();
		
		schedulingEntity.setPattern("* * * * *");
		schedulingEntity.setTaskAction("java:cm.homeautomation.sensors.base.test#testMethod");
		
		em.persist(schedulingEntity);
		
		em.getTransaction().commit();
		
		CustomTaskCollector customTaskCollector = new CustomTaskCollector();
		
		TaskTable tasks = customTaskCollector.getTasks();
		
		assertTrue(tasks.size()==1);
		
	}

}
