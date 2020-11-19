package cm.homeautomation.services.manualtask;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.ManualTask;
import cm.homeautomation.services.base.BaseService;

@Path("manualtask")
public class ManualTaskService extends BaseService {
	@Inject
	EntityManager em;
	
	@Inject
	ConfigurationService configurationService;
	@GET
	@Path("getAllOpen")
	public List<ManualTask> getAllOpen() {

		

		return em.createQuery("select t from ManualTask t where t.done=false", ManualTask.class).getResultList();
	}
}
