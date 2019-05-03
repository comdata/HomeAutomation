package cm.homeautomation.nashorn;

import java.util.List;

import javax.persistence.EntityManager;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class NashornRunner {

	private static NashornRunner instance;

	public static NashornRunner getInstance() {
		if (instance == null) {
			instance = new NashornRunner();
		}

		return instance;
	}

	public static void setInstance(final NashornRunner instance) {
		NashornRunner.instance = instance;
	}

	private final ScriptEngine engine;

	public NashornRunner() {
		final ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName("nashorn");

		setInstance(this);
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleEvent(final EventObject event) {
		Logger logger = LogManager.getLogger(this.getClass());
		final EntityManager em = EntityManagerService.getNewManager();

		final List<ScriptingEntity> resultList = em
				.createQuery("select se from ScriptingEntity se where se.scriptType=:scriptType", ScriptingEntity.class)
				.setParameter("scriptType", ScriptingEntity.ScriptingType.EVENTHANDLER).getResultList();

		if (resultList != null) {

			for (final ScriptingEntity scriptingEntity : resultList) {

				try {
					engine.eval(scriptingEntity.getJsCode());

					final Invocable invocable = (Invocable) engine;

					invocable.invokeFunction("eventFunction", event);
				} catch (final ScriptException e) {
					logger.error("ScriptException", e);
				} catch (final NoSuchMethodException e) {
					logger.error("NoSuchMethodException", e);
				}
			}

		}

		em.close();

	}

	public void run(final String jsCode) throws ScriptException {
		engine.eval(jsCode);
	}
}
