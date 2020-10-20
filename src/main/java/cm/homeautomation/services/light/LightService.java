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

@Path("light")
public class LightService extends BaseService {

	private static final String ON = "on";
	private static final String OFF = "off";
	private static final String DIMVALUE_CONST = "{DIMVALUE}";
	private static final String ZIGBEE = "ZIGBEE";
	private static final String LIGHT_ID = "lightId";
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
		return internalDimLight(lightId, dimValue, false, true);
	}

	public GenericStatus setLightState(long lightId, LightStates state, boolean isAbsoluteValue) {
		final EntityManager em = EntityManagerService.getManager();

		Light light = em.find(Light.class, lightId);

		int newDimValue = 0;

		if (light instanceof DimmableLight) {
			DimmableLight dimLight = (DimmableLight) light;
			if (state == LightStates.OFF) {
				newDimValue = dimLight.getMinimumValue();
			} else if (state == LightStates.ON) {
				if (dimLight.getBrightnessLevel() > dimLight.getMinimumValue()) {
					newDimValue = dimLight.getBrightnessLevel();
				} else {
					newDimValue = dimLight.getMaximumValue();
				}
			}
		} else {
			if (state == LightStates.OFF) {
				newDimValue = 0;
			} else if (state == LightStates.ON) {
				newDimValue = 100;
			}
		}

		return internalDimLight(lightId, newDimValue, false, isAbsoluteValue);
	}

	public Light getLightForTypeAndExternalId(final String type, final String externalId) {
		final EntityManager em = EntityManagerService.getManager();

		final List<Light> lights = em
				.createQuery("select l from Light l where l.lightType=:type and l.externalId=:externalId", Light.class)
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

	private GenericStatus internalDimLight(final long lightId, final int dimPercentValue, boolean calledForGroup,
			boolean isAbsoluteValue) {

		final Runnable requestThread = () -> {
			String powerState = getPowerStateFromDimValue(dimPercentValue);

			final EntityManager em = EntityManagerService.getManager();
			em.getTransaction().begin();
			final Light light = em.find(Light.class, lightId);

			if (light != null) {

				int dimValue = dimPercentValue;

//				LogManager.getLogger(this.getClass()).error("dimPercentValue: " + dimPercentValue);

				dimValue = dimByPercentIfPercentValue(dimPercentValue, isAbsoluteValue, light, dimValue);

				LogManager.getLogger(this.getClass()).error("dimValue: " + dimValue);

				// if part of a group then call for the others as well
				checkAndCallLightGroup(lightId, calledForGroup, isAbsoluteValue, em, light, dimValue);

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
					light.setPowerState(OFF.equals(powerState));
				}

				em.getTransaction().commit();

				sendMessageToDevice(powerState, light, dimValue, dimUrl);
			}
		};
		new Thread(requestThread).start();
		return new GenericStatus(true);
	}

	private String getPowerStateFromDimValue(final int dimPercentValue) {
		if (dimPercentValue == 0) {
			return OFF;
		} else {
			return ON;
		}
	}

	private void sendMessageToDevice(String powerState, final Light light, int dimValue, String dimUrl) {
		if (MQTT.equals(light.getLightType()) || ZIGBEE.equals(light.getLightType())) {
			String topic;
			String messagePayload;

			if (OFF.equals(powerState)) {
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
	}

	private void checkAndCallLightGroup(final long lightId, boolean calledForGroup, boolean isAbsoluteValue,
			final EntityManager em, final Light light, int dimValue) {
		if (!calledForGroup) {
			final String lightGroup = light.getLightGroup();

			if ((lightGroup != null) && !lightGroup.isEmpty()) {
				@SuppressWarnings("unchecked")
				final List<Light> resultList = em
						.createQuery("select l from Light l where l.id!=:lightId and l.lightGroup=:lightGroup")
						.setParameter(LIGHT_ID, lightId).setParameter("lightGroup", lightGroup).getResultList();

				if ((resultList != null) && !resultList.isEmpty()) {
					for (final Light lightGroupMember : resultList) {
						internalDimLight(lightGroupMember.getId(), dimValue, true, isAbsoluteValue);
					}
				}
			}
		}
	}

	private int dimByPercentIfPercentValue(final int dimPercentValue, boolean isAbsoluteValue, final Light light,
			int dimValue) {
		if (!isAbsoluteValue && light instanceof DimmableLight) {
			DimmableLight dimLight = (DimmableLight) light;

			if (dimLight.getMaximumValue() > 0 && dimLight.getMinimumValue() >= 0) {
				int range = dimLight.getMaximumValue() - dimLight.getMinimumValue();

				if (range > 0) {
					dimValue = dimLight.getMinimumValue() + ((range * dimPercentValue) / 100);
				}
			}

		}
		return dimValue;
	}

	@GET
	@Path("color/{lightId}/{hex}")
	public GenericStatus setColor(@PathParam(LIGHT_ID) final long lightId, @PathParam("hex") final String hex) {
		final String shortHex = hex.substring(1);
		EntityManager em = EntityManagerService.getManager();

		RGBLight rgbLight = findRGBLight(lightId, em);

		if (rgbLight != null) {

			rgbLight.setColor(hex);

			em.getTransaction().begin();
			em.persist(rgbLight);
			em.getTransaction().commit();

			if (MQTT.equals(rgbLight.getLightType()) || ZIGBEE.equals(rgbLight.getLightType())) {
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

	private RGBLight findRGBLight(final long lightId, EntityManager em) {
		return em.find(RGBLight.class, lightId);
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
