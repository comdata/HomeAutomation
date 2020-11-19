package cm.homeautomation.nashorn;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.script.ScriptException;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.services.base.BaseService;

@Path("admin/nashorn")
public class NashornService extends BaseService {

	@Inject
	NashornRunner nashornRunner;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@GET
	@Path("getAll")
	public List<ScriptingEntity> getAllEntities() {

		final List<ScriptingEntity> resultList = em
				.createQuery("select se from ScriptingEntity se", ScriptingEntity.class).getResultList();

		return resultList;
	}

	@GET
	@Path("run/{id}")
	public void run(@PathParam("id") Long id) throws ScriptException {
		final ScriptingEntity scriptingEntity = em.find(ScriptingEntity.class, id);

		nashornRunner.run(scriptingEntity.getJsCode());
	}

	@POST
	@Path("update")
	@Transactional
	public void updateEntity(ScriptingEntity entity) {

		ScriptingEntity modifiedEntity = entity;

		final List<ScriptingEntity> resultList = em
				.createQuery("select se from ScriptingEntity se where se.id=:id", ScriptingEntity.class)
				.setParameter("id", entity.getId()).getResultList();

		if (resultList != null) {
			modifiedEntity = resultList.get(0);
			modifiedEntity.setJsCode(entity.getJsCode());
			modifiedEntity.setName(entity.getName());

		}

		em.persist(modifiedEntity);
	}

	@GET
	@Path("enable")
	public void enableService() {
		String group = "nashorn";
		String property = "enabled";
		String value = "true";
		configurationService.createOrUpdate(group, property, value);
		nashornRunner.enableEngine(value);
	}

	@GET
	@Path("disable")
	public void disableService() {
		String group = "nashorn";
		String property = "enabled";
		String value = "true";
		configurationService.createOrUpdate(group, property, value);
		nashornRunner.stopEngine();
	}
}
