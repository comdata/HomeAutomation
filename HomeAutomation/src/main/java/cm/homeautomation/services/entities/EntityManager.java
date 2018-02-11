package cm.homeautomation.services.entities;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("entityManager")
public class EntityManager extends BaseService {

	@GET
	@Path("evict")
	public GenericStatus evictCache() {
		EntityManagerService.getManager().getEntityManagerFactory().getCache().evictAll();

		return new GenericStatus(true);
	}
}
