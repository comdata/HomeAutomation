package cm.homeautomation.nashorn;

import java.util.List;

import javax.persistence.EntityManager;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.AutoCreateInstance;
import org.apache.logging.log4j.Logger;

@AutoCreateInstance
public class NashhornRunner {

	private ScriptEngine engine;
	private static NashhornRunner instance;
	private Logger logger=LogManager.getLogger(this.getClass());

	public NashhornRunner() {
		ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName("nashorn");
		
		instance=this;
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleEvent(EventObject event) {

		EntityManager em = EntityManagerService.getNewManager();

		List<ScriptingEntity> resultList = (List<ScriptingEntity>) em
				.createQuery("select se from ScriptingEntity se where se.scriptType=:scriptType").setParameter("scriptType", ScriptingEntity.ScriptingType.EVENTHANDLER).getResultList();

		if (resultList != null) {

			for (ScriptingEntity scriptingEntity : resultList) {

				try {
					engine.eval(scriptingEntity.getJsCode());

					Invocable invocable = (Invocable) engine;

					invocable.invokeFunction("eventFunction", event);
				} catch (ScriptException e) {
					logger.error("ScriptException", e);
				} catch (NoSuchMethodException e) {
					logger.error("NoSuchMethodException", e);
				}
			}

		}

		em.close();

	}

	public static void main(String[] args) throws Exception {
		// Script Engine Manager

		// Evaluate JavaScript code
		getInstance().run("print(\"This is a hello from JavaScript in Java\");");
	}

	public void run(String jsCode) throws ScriptException {
		engine.eval(jsCode);
	}

	public static NashhornRunner getInstance() {
		if (instance == null) {
			instance = new NashhornRunner();
		}

		return instance;
	}

	public static void setInstance(NashhornRunner instance) {
		NashhornRunner.instance = instance;
	}
}
