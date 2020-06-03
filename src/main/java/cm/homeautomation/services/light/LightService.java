package cm.homeautomation.services.light;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.entities.Room;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.base.HTTPHelper;
import cm.homeautomation.tradfri.TradfriStartupService;

@Path("light")
public class LightService extends BaseService {

	private static final String DIMVALUE_CONST = "{DIMVALUE}";
	private static final String ZIGBEE = "ZIGBEE";
	private static final String LIGHT_ID = "lightId";
	private static final String TRADFRI = "TRADFRI";
	private static final String MQTT = "MQTT";

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

		final EntityManager em = EntityManagerService.getManager();
		em.getTransaction().begin();

		final Room room = (Room) em.createQuery("select r from Room r where r.id=:roomId")
				.setParameter("roomId", roomId).getSingleResult();

		room.getLights().add(light);
		light.setRoom(room);

		em.persist(light);

		em.getTransaction().commit();

		return light;
	}

	@GET
	@Path("dim/{lightId}/{dimValue}")
	public GenericStatus dimLight(@PathParam(LIGHT_ID) final long lightId, @PathParam("dimValue") int dimValue) {
		return internalDimLight(lightId, dimValue, false);
	}

	public GenericStatus setLightState(long lightId, LightStates state) {
		final EntityManager em = EntityManagerService.getManager();

		Light light = em.find(Light.class, lightId);

		int newDimValue = 0;

		if (light instanceof DimmableLight) {
			DimmableLight dimLight = (DimmableLight) light;
			switch (state) {
			case OFF:
				newDimValue = dimLight.getMinimumValue();
				break;
			case ON:
				newDimValue = dimLight.getBrightnessLevel();
				break;
			}
		} else {
			switch (state) {
			case OFF:
				newDimValue = 0;
				break;
			case ON:
				newDimValue = 100;
				break;
			}
		}

		return internalDimLight(lightId, newDimValue, false);
	}

	public Light getLightForTypeAndExternalId(final String type, final String externalId) {
		final EntityManager em = EntityManagerService.getManager();

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

		final EntityManager em = EntityManagerService.getManager();
		@SuppressWarnings("unchecked")
		final List<Light> resultList = em
				.createQuery("select l from Light l where l.room=(select r from Room r where r.id=:roomId)")
				.setParameter("roomId", roomId).getResultList();

		return resultList;
	}

	private GenericStatus internalDimLight(final long lightId, final int dimPercentValue, boolean calledForGroup) {

		final Runnable httpRequestThread = () -> {
			String powerState = "off";

			if (dimPercentValue == 0) {
				powerState = "off";
			} else {
				powerState = "on";
			}

			final EntityManager em = EntityManagerService.getManager();
			em.getTransaction().begin();
			final Light light = (Light) em.createQuery("select l from Light l where l.id=:lightId")
					.setParameter(LIGHT_ID, lightId).getSingleResult();

			int dimValue = dimPercentValue;

			LogManager.getLogger(this.getClass()).error("dimPercentValue: " + dimPercentValue);

			if (light instanceof DimmableLight) {
				DimmableLight dimLight = (DimmableLight) light;

				if (dimLight.getMaximumValue() > 0 && dimLight.getMinimumValue() >= 0) {
					int range = dimLight.getMaximumValue() - dimLight.getMinimumValue();

					if (range > 0) {
						dimValue = dimLight.getMinimumValue()
								+ ((range * dimPercentValue) / 100);
					}
				}

			}

			LogManager.getLogger(this.getClass()).error("dimValue: " + dimValue);

			// if part of a group then call for the others as well
			if (!calledForGroup) {
				final String lightGroup = light.getLightGroup();

				if ((lightGroup != null) && !lightGroup.isEmpty()) {
					@SuppressWarnings("unchecked")
					final List<Light> resultList = em
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
			}
			if (MQTT.equals(light.getLightType()) || ZIGBEE.equals(light.getLightType())) {
				String topic;
				String messagePayload;

				if ("off".equals(powerState)) {
					topic = light.getMqttPowerOffTopic();
					messagePayload = light.getMqttPowerOffMessage();
				} else {
					topic = light.getMqttPowerOnTopic();
					messagePayload = light.getMqttPowerOnMessage();

					messagePayload = messagePayload.replace(DIMVALUE_CONST, Integer.toString(dimValue));
				}

				MQTTSender.sendMQTTMessage(topic, messagePayload);
			} else {

				dimUrl = dimUrl.replace(DIMVALUE_CONST, Integer.toString(dimValue));
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
		final String shortHex = hex.substring(1);
		final EntityManager em = EntityManagerService.getManager();

		RGBLight rgbLight = em.find(RGBLight.class, lightId);

		if (rgbLight != null) {

			rgbLight.setColor(hex);

			em.getTransaction().begin();
			em.persist(rgbLight);
			em.getTransaction().commit();

			if (TRADFRI.equals(rgbLight.getLightType())) {
				TradfriStartupService.getInstance().setColor(rgbLight.getExternalId(), shortHex);
			} else if (MQTT.equals(rgbLight.getLightType()) || ZIGBEE.equals(rgbLight.getLightType())) {
				if (rgbLight.getMqttColorMessage() != null && rgbLight.getMqttColorTopic() != null) {
					String topic = rgbLight.getMqttColorTopic();
					String messagePayload = rgbLight.getMqttColorMessage().replace("{HEXVALUE}", shortHex);
					MQTTSender.sendMQTTMessage(topic, messagePayload);
				}
			} else {
				String colorUrl = rgbLight.getColorUrl();
				if (colorUrl != null) {
					colorUrl = colorUrl.replace("{HEXVALUE}", shortHex);
					HTTPHelper.performHTTPRequest(colorUrl);
				}
			}
		}

		return new GenericStatus(true);
	}

	public void setColor(long lightId, int dimValue, Float x, Float y) {
		final EntityManager em = EntityManagerService.getManager();

		RGBLight rgbLight = em.find(RGBLight.class, lightId);

		if (rgbLight != null && rgbLight.getMqttColorMessage() != null && rgbLight.getMqttColorTopic() != null) {

			String topic = rgbLight.getMqttColorTopic();
			String messagePayload = rgbLight.getMqttColorMessage().replace(DIMVALUE_CONST, Integer.toString(dimValue))
					.replace("{colorX}", x.toString()).replace("{colorY}", y.toString());
			MQTTSender.sendMQTTMessage(topic, messagePayload);
		}

	}

}
