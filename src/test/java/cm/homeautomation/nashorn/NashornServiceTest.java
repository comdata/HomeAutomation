package cm.homeautomation.nashorn;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.entities.ScriptingEntity.ScriptingType;

public class NashornServiceTest {

	@BeforeEach
	public void setup() {
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		int executeUpdate = em.createQuery("delete from ScriptingEntity se").executeUpdate();
		em.getTransaction().commit();
	}
	
	@Test
	public void testGetAllEntities() throws Exception {
		String script = "var eventFunction=function (eventObject) { print(eventObject.data.testData); eventObject.data.testResultData=eventObject.data.testData};";
		ScriptingEntity scriptingEntity = new ScriptingEntity();
		scriptingEntity.setJsCode(script);
		scriptingEntity.setScriptType(ScriptingType.EVENTHANDLER);
		scriptingEntity.setName("HANDLE EVENT TEST");
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		em.persist(scriptingEntity);
		em.getTransaction().commit();
		
		NashornService nashornService = new NashornService();
		
		List<ScriptingEntity> allEntities = nashornService.getAllEntities();
		
		assertNotNull(allEntities);
		assertFalse(allEntities.isEmpty());
		System.out.println(allEntities.size());
		assertTrue(allEntities.size()==1);
	}

	@Test
	public void testRun() throws Exception {
		String script = "print ('Hello World')";
		ScriptingEntity scriptingEntity = new ScriptingEntity();
		scriptingEntity.setJsCode(script);
		scriptingEntity.setScriptType(ScriptingType.UIACTION);
		scriptingEntity.setName("HANDLE EVENT TEST");
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		em.persist(scriptingEntity);
		em.getTransaction().commit();
		
		NashornService nashornService = new NashornService();
		
		List<ScriptingEntity> allEntities = nashornService.getAllEntities();
		assertNotNull(allEntities);
		
		ScriptingEntity scriptingEntityForChange = allEntities.get(0);
		
		nashornService.run(scriptingEntityForChange.getId());
	}

	@Test
	public void testUpdateEntity() throws Exception {
		String script = "var eventFunction=function (eventObject) { print(eventObject.data.testData); eventObject.data.testResultData=eventObject.data.testData};";
		ScriptingEntity scriptingEntity = new ScriptingEntity();
		scriptingEntity.setJsCode(script);
		scriptingEntity.setScriptType(ScriptingType.EVENTHANDLER);
		scriptingEntity.setName("HANDLE EVENT TEST");
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		em.persist(scriptingEntity);
		em.getTransaction().commit();
		
		NashornService nashornService = new NashornService();
		
		List<ScriptingEntity> allEntities = nashornService.getAllEntities();
		assertNotNull(allEntities);
		
		ScriptingEntity scriptingEntityForChange = allEntities.get(0);
		
		scriptingEntityForChange.setName("MODIFIED TEST EVENT");
		em.getTransaction().begin();
		
		em.merge(scriptingEntityForChange);
		
		em.getTransaction().commit();
	
		
		List<ScriptingEntity> allEntitiesChanged = nashornService.getAllEntities();
		assertNotNull(allEntitiesChanged);
		
		ScriptingEntity scriptingEntityChanged = allEntities.get(0);
		
		assertTrue("MODIFIED TEST EVENT".equals(scriptingEntityChanged.getName()));
		
	}

}
