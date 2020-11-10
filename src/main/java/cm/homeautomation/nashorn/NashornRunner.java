package cm.homeautomation.nashorn;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.eventbus.EventObject;
import io.quarkus.vertx.ConsumeEvent;

@Singleton
public class NashornRunner {

	private static final String ENABLED = "enabled";
	private static final String NASHORN = "nashorn";
	private static final String TRUE = "true";

	private ScriptEngine engine = null;

	public NashornRunner() {
		String nashornEnabled = ConfigurationService.getConfigurationProperty(NASHORN, ENABLED);

		enableEngine(nashornEnabled);
	}

	public void enableEngine(String nashornEnabled) {
		if (TRUE.equalsIgnoreCase(nashornEnabled)) {
			final ScriptEngineManager factory = new ScriptEngineManager();
			engine = factory.getEngineByName(NASHORN);
		}
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handleEvent(final EventObject event) {
		Logger logger = LogManager.getLogger(this.getClass());
		if (engine != null) {

			final EntityManager em = EntityManagerService.getManager();

			final List<ScriptingEntity> resultList = em
					.createQuery("select se from ScriptingEntity se where se.scriptType=:scriptType",
							ScriptingEntity.class)
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

		} else {
			logger.debug("nashorn engine not enabled");
		}
	}

	public void run(final String jsCode) throws ScriptException {
		engine.eval(jsCode);
	}

	public void stopEngine() {
		engine = null;
	}
}
