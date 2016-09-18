package cm.homeautomation.services.cameras;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Camera;
import cm.homeautomation.services.base.BaseService;

@Path("cameras")
public class CameraService extends BaseService {

	@Path("getAll")
	public List<Camera> getAll() {
		EntityManager em = EntityManagerService.getManager();
		
		List<Camera> resultList = (List<Camera>)em.createQuery("select c from Camera c").getResultList();
		return resultList;
	}
}
