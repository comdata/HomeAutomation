package cm.homeautomation.services.dashbuttons;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;

@Path("dashbutton")
public class DashbuttonConfigurationService {

	@Path("readAll")
	@GET
	public List<DashButton> getDashbuttons() {
		
		EntityManager em = EntityManagerService.getManager();
		
		return em.createQuery("select db from DashButton db", DashButton.class).getResultList();
	}
	
}
