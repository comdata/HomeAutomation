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
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.AutoCreateInstance;
import io.quarkus.vertx.ConsumeEvent;

@AutoCreateInstance
public class NashornRunner {

	private static final String ENABLED = "enabled";
	private static final String NASHORN = "nashorn";
	private static final String TRUE = "true";
	private static NashornRunner instance;
	private ScriptEngine engine = null;

	public static NashornRunner getInstance() {
		if (instance == null) {
			instance = new NashornRunner();
		}

		return instance;
	}

	public static void setInstance(final NashornRunner instance) {
		NashornRunner.instance = instance;
	}

	public NashornRunner() {
		String nashornEnabled = ConfigurationService.getConfigurationProperty(NASHORN, ENABLED);

		if (TRUE.equalsIgnoreCase(nashornEnabled)) {
			final ScriptEngineManager factory = new ScriptEngineManager();
			engine = factory.getEngineByName(NASHORN);

			setInstance(this);
			EventBusService.getEventBus().register(this);
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

	public static void stopInstance() {
		if (instance != null) {
			EventBusService.getEventBus().unregister(instance);
			instance = null;
		}

	}
}
