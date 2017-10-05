package cm.homeautomation.nashorn;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.services.base.BaseService;

@Path("admin/nashorn")
public class NashornService extends BaseService {

	@GET
	@Path("getAll")
	public List<ScriptingEntity> getAllEntities() {
		EntityManager em = EntityManagerService.getNewManager();

		List<ScriptingEntity> resultList = (List<ScriptingEntity>) em
				.createQuery("select se from ScriptingEntity se").getResultList();

		return resultList;
	}

	@POST
	@Path("update")
	public void updateEntity(ScriptingEntity entity) {
		EntityManager em = EntityManagerService.getNewManager();

		ScriptingEntity modifiedEntity=entity;
		
		List<ScriptingEntity> resultList = (List<ScriptingEntity>) em
				.createQuery("select se from ScriptingEntity se where se.id=:id").setParameter("id", entity.getId()).getResultList();

		
		if (resultList!=null) {
			
		}
		
	}
	
}
