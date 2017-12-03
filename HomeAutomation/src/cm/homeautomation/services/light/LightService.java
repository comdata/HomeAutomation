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
import cm.homeautomation.tradfri.TradfriStartupService;

@Path("light")
public class LightService extends BaseService {

	private static LightService instance;

	public static LightService getInstance() {
		if (instance == null) {
			instance = new LightService();
		}
		return instance;
	}

	public static void setInstance(final LightService instance) {
		LightService.instance = instance;
	}

	public LightService() {
		setInstance(this);
	}

	@GET
	@Path("create/{name}/{lightType}/{roomId}")
	public Light createLight(@PathParam("name") final String name, @PathParam("lightType") final String lightType,
			@PathParam("roomId") final long roomId) {

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

		final EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		final Room room = (Room) em.createQuery("select r from Room r where r.id=:roomId")
				.setParameter("roomId", roomId).getSingleResult();

		room.getLights().add(light);
		light.setRoom(room);

		em.persist(light);

		em.getTransaction().commit();
		em.close();
		return light;
	}

	@GET
	@Path("dim/{lightId}/{dimValue}")
	public GenericStatus dimLight(@PathParam("lightId") final long lightId, @PathParam("dimValue") int dimValue) {
		String powerState = "off";

		if (dimValue == 0) {
			powerState = "off";
		} else {
			powerState = "on";
		}

		final EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		final Light light = (Light) em.createQuery("select l from Light l where l.id=:lightId")
				.setParameter("lightId", lightId).getSingleResult();

		String dimUrl = light.getDimUrl();

		if (light instanceof DimmableLight) {
			final DimmableLight dimmableLight = (DimmableLight) light;

			if (dimValue > dimmableLight.getMaximumValue()) {
				dimValue = dimmableLight.getMaximumValue();
			}

			dimmableLight.setBrightnessLevel(dimValue);
			em.persist(dimmableLight);
			dimUrl = dimmableLight.getDimUrl();
		} else {
			light.setPowerState(("off".equals(powerState)) ? false : true);
		}

		em.getTransaction().commit();

		if ("TRADFRI".equals(light.getLightType())) {
			TradfriStartupService.getInstance().dimBulb(light.getExternalId(), dimValue);
		} else {

			dimUrl = dimUrl.replace("{DIMVALUE}", Integer.toString(dimValue));
			dimUrl = dimUrl.replace("{STATE}", powerState);

			HTTPHelper.performHTTPRequest(dimUrl);
		}
		return new GenericStatus(true);
	}

	public Light getLightForTypeAndExternalId(final String type, final String externalId) {
		final EntityManager em = EntityManagerService.getNewManager();

		@SuppressWarnings("unchecked")
		final List<Light> lights = em
				.createQuery("select l from Light l where l.lightType=:type and l.externalId=:externalId")
				.setParameter("type", type).setParameter("externalId", externalId).getResultList();

		if ((lights != null) && !lights.isEmpty()) {
			for (final Light light : lights) {
				return light;
			}
		}

		return null;
	}

	@GET
	@Path("get/{roomId}")
	public List<Light> getLights(@PathParam("roomId") final Long roomId) {

		final EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		final List<Light> resultList = em
				.createQuery("select l from Light l where l.room=(select r from Room r where r.id=:roomId)")
				.setParameter("roomId", roomId).getResultList();

		return resultList;
	}

	@GET
	@Path("color/{lightId}/{hex}")
	public GenericStatus setColor(@PathParam("lightId") final long lightId, @PathParam("hex") final String hex) {
		final EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		final Light light = (Light) em.createQuery("select l from Light l where l.id=:lightId")
				.setParameter("lightId", lightId).getSingleResult();

		if (light instanceof RGBLight) {
			final RGBLight colorLight = (RGBLight) light;

			colorLight.setColor(hex);

			em.persist(colorLight);

			String colorUrl = colorLight.getColorUrl();
			if (colorUrl != null) {
				colorUrl = colorUrl.replace("{HEXVALUE}", hex);
				HTTPHelper.performHTTPRequest(colorUrl);
			}

		}
		em.getTransaction().commit();

		return new GenericStatus(true);
	}

}
