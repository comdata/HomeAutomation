package cm.homeautomation.services.dashbuttons;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.services.base.BaseService;

@Path("dashbutton")
public class DashbuttonConfigurationService extends BaseService {

	@Path("readAll")
	@GET
	public GenericEntity<List<DashButton>> getDashbuttons() {
		
		EntityManager em = EntityManagerService.getManager();
		
		List<DashButton> resultList=em.createQuery("select db from DashButton db", DashButton.class).getResultList();
		
		return new GenericEntity<List<DashButton>>(resultList) {};
	}
	
}
