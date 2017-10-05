package cm.homeautomation.nashorn;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.services.base.BaseService;

@Path("admin/nas")
public class NashornService extends BaseService {

	@GET
	@Path("getAll")
	public List<ScriptingEntity> getAllEntities() {
		EntityManager em = EntityManagerService.getNewManager();

		List<ScriptingEntity> resultList = (List<ScriptingEntity>) em
				.createQuery("select se from ScriptingEntity se").getResultList();

		return resultList;
	}


	
}
