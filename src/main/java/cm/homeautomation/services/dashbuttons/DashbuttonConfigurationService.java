package cm.homeautomation.services.dashbuttons;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.services.base.BaseService;

@Path("dashbutton")
public class DashbuttonConfigurationService extends BaseService {

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Path("readAll")
	@GET
	public GenericEntity<List<DashButton>> getDashbuttons() {

		List<DashButton> resultList = em.createQuery("select db from DashButton db", DashButton.class).getResultList();

		return new GenericEntity<List<DashButton>>(resultList) {
		};
	}

	@Path("update")
	@POST
	
	public void updateDashButton(DashButton dashbutton) {
		em.merge(dashbutton);
	}

	@GET
	@Path("new/{name}/{mac}")
	
	public DashButton createNewDashButton(@PathParam("name") String name, @PathParam("mac") String mac) {

		DashButton dashButton = new DashButton();
		dashButton.setName(name);
		dashButton.setMac(mac.replace(":", "").toUpperCase());
		em.persist(dashButton);

		return dashButton;

	}

}
