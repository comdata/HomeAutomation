package cm.homeautomation.nashorn;

import java.util.List;

import javax.persistence.EntityManager;
import javax.script.ScriptException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.services.base.BaseService;

@Path("admin/nashorn")
public class NashornService extends BaseService {

	@GET
	@Path("getAll")
	public List<ScriptingEntity> getAllEntities() {
		final EntityManager em = EntityManagerService.getNewManager();

		final List<ScriptingEntity> resultList = em.createQuery("select se from ScriptingEntity se", ScriptingEntity.class).getResultList();

		return resultList;
	}

	@GET
	@Path("run/{id}")
	public void run(@PathParam("id") Long id) throws ScriptException {
		final EntityManager em = EntityManagerService.getNewManager();

		final ScriptingEntity scriptingEntity = em.find(ScriptingEntity.class, id);

		NashornRunner.getInstance().run(scriptingEntity.getJsCode());
	}

	@POST
	@Path("update")
	public void updateEntity(ScriptingEntity entity) {
		final EntityManager em = EntityManagerService.getNewManager();

		em.getTransaction().begin();
		ScriptingEntity modifiedEntity = entity;

		final List<ScriptingEntity> resultList = em.createQuery("select se from ScriptingEntity se where se.id=:id", ScriptingEntity.class)
				.setParameter("id", entity.getId()).getResultList();

		if (resultList != null) {
			modifiedEntity = resultList.get(0);
			modifiedEntity.setJsCode(entity.getJsCode());
			modifiedEntity.setName(entity.getName());

		}

		em.persist(modifiedEntity);
		em.getTransaction().commit();

	}

}
