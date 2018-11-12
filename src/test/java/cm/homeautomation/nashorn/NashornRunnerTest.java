package cm.homeautomation.nashorn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.entities.ScriptingEntity.ScriptingType;
import cm.homeautomation.eventbus.EventObject;

public class NashornRunnerTest {

	@BeforeEach
	public void setup() {
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		em.createQuery("delete from ScriptingEntity").executeUpdate();
		em.getTransaction().commit();
	}
	
	@Test
	public void testGetInstance() throws Exception {
		NashornRunner.setInstance(null);
		assertNotNull(NashornRunner.getInstance());
	}

	@Test
	public void testSetInstance() throws Exception {
		NashornRunner newInstance = new NashornRunner();
		NashornRunner.setInstance(null);
		NashornRunner.setInstance(newInstance);
		assertNotNull(NashornRunner.getInstance());
		assertEquals(NashornRunner.getInstance(), newInstance);
	}

	@Test
	public void testNashornRunner() throws Exception {
		NashornRunner nashornRunner = new NashornRunner();
		assertNotNull(nashornRunner);
		assertNotNull(NashornRunner.getInstance());
	}

	@Test
	public void testHandleEvent() throws Exception {
		
		String script = "var eventFunction=function (eventObject) { print(eventObject.data.testData); eventObject.data.testResultData=eventObject.data.testData};";
		ScriptingEntity scriptingEntity = new ScriptingEntity();
		scriptingEntity.setJsCode(script);
		scriptingEntity.setScriptType(ScriptingType.EVENTHANDLER);
		scriptingEntity.setName("HANDLE EVENT TEST");
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		em.persist(scriptingEntity);
		em.getTransaction().commit();
		
		NashornRunner instance = NashornRunner.getInstance();

		NashhornRunnerTestEvent data = new NashhornRunnerTestEvent();
		String testData = "Hello World";
		data.setTestData(testData);

		instance.handleEvent(new EventObject(data));

		assertEquals(data.getTestResultData(), testData);
	}
}
