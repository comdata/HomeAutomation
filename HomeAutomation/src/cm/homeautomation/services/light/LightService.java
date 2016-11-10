package cm.homeautomation.services.light;

import javax.persistence.EntityManager;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.entities.Room;
import cm.homeautomation.services.base.BaseService;

@Path("light")
public class LightService extends BaseService {

	public Light createLight(String name, String lightType, long roomId) {

		Light light = null;

		switch (lightType) {
		case "LIGHT":
			light=new Light();
			break;
		case "DIMMABLELIGHT":
			light=new DimmableLight();
			break;
		case "RGBLIGHT":
			light=new RGBLight();
			break;

		}
		light.setName(name);
		
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		Room room = (Room) em.createQuery("select r from Room r where id=:roomId").setParameter("roomId", roomId).getSingleResult();
		
		room.getLights().add(light);
		light.setRoom(room);
		
		em.persist(light);
		
		em.getTransaction().commit();

		return light;
	}

}
