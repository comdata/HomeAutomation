package cm.homeautomation.services.light;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.entities.Room;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.base.HTTPHelper;

@Path("light")
public class LightService extends BaseService {

	@GET
	@Path("create/{name}/{lightType}/{roomId}")
	public Light createLight(@PathParam("name") String name, @PathParam("lightType") String lightType,
			@PathParam("roomId") long roomId) {

		Light light = null;

		switch (lightType) {
		case "LIGHT":
			light = new Light();
			break;
		case "DIMMABLELIGHT":
			light = new DimmableLight();
			break;
		case "RGBLIGHT":
			light = new RGBLight();
			break;

		}
		light.setName(name);

		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		Room room = (Room) em.createQuery("select r from Room r where r.id=:roomId").setParameter("roomId", roomId)
				.getSingleResult();

		room.getLights().add(light);
		light.setRoom(room);

		em.persist(light);

		em.getTransaction().commit();
		em.close();
		return light;
	}

	@GET
	@Path("dim/{lightId}/{dimValue}")
	public GenericStatus dimLight(@PathParam("lightId") long lightId, @PathParam("dimValue") int dimValue) {
		String powerState="off";
		
		if (dimValue>99) {
			dimValue=99;
		}
		
		if (dimValue==0) {
			powerState="off";
		}else {
			powerState="on";
		}
		
		
		
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		Light light = (Light) em.createQuery("select l from Light l where l.id=:lightId")
				.setParameter("lightId", lightId).getSingleResult();

		String dimUrl = light.getDimUrl();
		
		if (light instanceof DimmableLight) {
			DimmableLight dimmableLight=(DimmableLight)light;
			dimmableLight.setBrightnessLevel(dimValue);
			em.persist(dimmableLight);		
			dimUrl = dimmableLight.getDimUrl();
		} else {
			light.setPowerState(("off".equals(powerState))?false:true);
		}
		

		
		em.getTransaction().commit();
		
		
		
		dimUrl = dimUrl.replace("{DIMVALUE}", Integer.toString(dimValue));
		dimUrl = dimUrl.replace("{STATE}", powerState);
		
		HTTPHelper.performHTTPRequest(dimUrl);
		
		return new GenericStatus(true);
	}
	
	@GET
	@Path("get/{roomId}")
	public List<Light> getLights(@PathParam("roomId") Long roomId) {
		
		EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<Light> resultList = (List<Light>)em.createQuery("select l from Light l where l.room=(select r from Room r where r.id=:roomId)")
				.setParameter("roomId", roomId).getResultList();
		
		return resultList;
	}

}
