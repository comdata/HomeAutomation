package cm.homeautomation.services.dashbuttons;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

		List<DashButton> resultList = em.createQuery("select db from DashButton db", DashButton.class).getResultList();

		return new GenericEntity<List<DashButton>>(resultList) {
		};
	}

	@Path("update")
	@POST
	public void updateDashButton(DashButton dashbutton) {
		EntityManager em = EntityManagerService.getManager();

		em.getTransaction().begin();
		
		em.merge(dashbutton);
		
		em.getTransaction().commit();
	}

	@GET
	@Path("new/{name}/{mac}")
	public DashButton createNewDashButton(@PathParam("name") String name, @PathParam("mac") String mac) {
		EntityManager em = EntityManagerService.getManager();

		em.getTransaction().begin();

		DashButton dashButton = new DashButton();
		dashButton.setName(name);
		dashButton.setMac(mac.replace(":", "").toUpperCase());
		em.persist(dashButton);
		em.getTransaction().commit();

		return dashButton;

	}

}
