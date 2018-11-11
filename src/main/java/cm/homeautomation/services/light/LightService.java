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

	private static final String LIGHT_ID = "lightId";
	private static final String TRADFRI = "TRADFRI";
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
		default:
			return null;
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
	public GenericStatus dimLight(@PathParam(LIGHT_ID) final long lightId, @PathParam("dimValue") int dimValue) {
		return internalDimLight(lightId, dimValue, false);
	}

	public Light getLightForTypeAndExternalId(final String type, final String externalId) {
		final EntityManager em = EntityManagerService.getNewManager();

		@SuppressWarnings("unchecked")
		final List<Light> lights = em
				.createQuery("select l from Light l where l.lightType=:type and l.externalId=:externalId")
				.setParameter("type", type).setParameter("externalId", externalId).getResultList();

		if ((lights != null) && !lights.isEmpty()) {
			return lights.get(0);
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

	private GenericStatus internalDimLight(final long lightId, final int dimValue, boolean calledForGroup) {

		final Runnable httpRequestThread = () -> {
			String powerState = "off";

			if (dimValue == 0) {
				powerState = "off";
			} else {
				powerState = "on";
			}

			final EntityManager em = EntityManagerService.getNewManager();
			em.getTransaction().begin();
			final Light light = (Light) em.createQuery("select l from Light l where l.id=:lightId")
					.setParameter(LIGHT_ID, lightId).getSingleResult();

			// if part of a group then call for the others as well
			if (!calledForGroup) {
				final String lightGroup = light.getLightGroup();

				if ((lightGroup != null) && !lightGroup.isEmpty()) {
					@SuppressWarnings("unchecked")
					final List<Light> resultList = (List<Light>)em
							.createQuery("select l from Light l where l.id!=:lightId and l.lightGroup=:lightGroup")
							.setParameter(LIGHT_ID, lightId).setParameter("lightGroup", lightGroup).getResultList();

					if ((resultList != null) && !resultList.isEmpty()) {
						for (final Light lightGroupMember : resultList) {
							internalDimLight(lightGroupMember.getId(), dimValue, true);
						}
					}
				}
			}

			String dimUrl = light.getDimUrl();

			if (light instanceof DimmableLight) {
				final DimmableLight dimmableLight = (DimmableLight) light;

				int newDimValue = dimValue;
				if (newDimValue > dimmableLight.getMaximumValue()) {
					newDimValue = dimmableLight.getMaximumValue();
				}

				dimmableLight.setBrightnessLevel(newDimValue);
				em.persist(dimmableLight);
				dimUrl = dimmableLight.getDimUrl();
			} else {
				light.setPowerState("off".equals(powerState));
			}

			em.getTransaction().commit();

			if (TRADFRI.equals(light.getLightType())) {
				TradfriStartupService.getInstance().dimBulb(light.getExternalId(), dimValue);
			} else {

				dimUrl = dimUrl.replace("{DIMVALUE}", Integer.toString(dimValue));
				dimUrl = dimUrl.replace("{STATE}", powerState);

				HTTPHelper.performHTTPRequest(dimUrl);
			}
		};
		new Thread(httpRequestThread).start();
		return new GenericStatus(true);
	}

	@GET
	@Path("color/{lightId}/{hex}")
	public GenericStatus setColor(@PathParam(LIGHT_ID) final long lightId, @PathParam("hex") final String hex) {
		final EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		final Light light = (Light) em.createQuery("select l from Light l where l.id=:lightId")
				.setParameter(LIGHT_ID, lightId).getSingleResult();

		if (light instanceof RGBLight) {
			final RGBLight colorLight = (RGBLight) light;

			colorLight.setColor(hex);

			final String shortHex = hex.substring(1);

			em.persist(colorLight);

			String colorUrl = colorLight.getColorUrl();

			if (TRADFRI.equals(colorLight.getLightType())) {
				TradfriStartupService.getInstance().setColor(light.getExternalId(), shortHex);
			} else {
				if (colorUrl != null) {
					colorUrl = colorUrl.replace("{HEXVALUE}", shortHex);
					HTTPHelper.performHTTPRequest(colorUrl);
				}
			}
		}
		em.getTransaction().commit();

		return new GenericStatus(true);
	}

}
