package cm.homeautomation.services.hueinterface;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.HueDevice;
import cm.homeautomation.entities.HueDeviceType;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.services.actor.ActorService;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.light.LightService;
import cm.homeautomation.services.light.LightStates;
import cm.homeautomation.services.windowblind.WindowBlindService;

@Path("hueInterface")
public class HueInterface extends BaseService {

	@POST
	@Path("send")
	public GenericStatus handleMessage(HueEmulatorMessage message) {
		EntityManager em = EntityManagerService.getManager();

		String lightId = message.getLightId();
		List<HueDevice> hueDeviceList = em.createQuery("select hd from HueDevice hd where hd.lightId=:lightId",
				HueDevice.class)
				.setParameter("lightId", lightId).getResultList();

		if (hueDeviceList == null || hueDeviceList.isEmpty()) {

			List<HueDevice> hueDeviceNameList = em.createQuery("select hd from HueDevice hd where hd.name=:name",
					HueDevice.class)
					.setParameter("name", message.getDeviceName()).getResultList();
			if (hueDeviceNameList != null && !hueDeviceNameList.isEmpty()) {
				HueDevice hueDevice = hueDeviceNameList.get(0);

				em.getTransaction().begin();
				hueDevice.setLightId(message.getLightId());
				em.merge(hueDevice);
				em.getTransaction().commit();
				handleMessage(message);
			} else {

				// try to find existing device
				long externalId = 0;
				HueDeviceType type = null;

				List<Switch> switchList = em.createQuery("select sw from Switch sw where sw.name=:name", Switch.class)
						.setParameter("name", message.getDeviceName()).getResultList();

				if (switchList != null && !switchList.isEmpty()) {
					Switch singleSwitch = switchList.get(0);
					externalId = singleSwitch.getId();
					type = HueDeviceType.SWITCH;
				} else {
					List<Light> lightList = em.createQuery("select l from Light l where l.name=:name", Light.class)
							.setParameter("name", message.getDeviceName()).getResultList();
					if (lightList != null && !lightList.isEmpty()) {
						Light singleLight = lightList.get(0);
						externalId = singleLight.getId();
						type = HueDeviceType.LIGHT;
					}
				}
				em.getTransaction().begin();

				HueDevice hueDevice = new HueDevice();
				hueDevice.setName(message.getDeviceName());
				hueDevice.setLightId(message.getLightId());

				if (externalId > 0 && type != null) {
					hueDevice.setExternalId(externalId);
					hueDevice.setType(type);

				}

				em.persist(hueDevice);
				em.getTransaction().commit();

				if (externalId > 0 && type != null) {
					// do it again, since device is now created
					handleMessage(message);
				}
			}
		} else {
			HueDevice hueDevice = hueDeviceList.get(0);

			if (hueDevice != null) {

				if (message.getXy() != null) {
					System.out.println("Color infos:" + message.getXy()[0] + " - " + message.getXy()[1]);
				}

				HueDeviceType type = hueDevice.getType();
				if (type != null) {
					switch (type) {
					case LIGHT:
						LightService.getInstance().setLightState(hueDevice.getExternalId(),
								("on".equals(message.getPayload()) ? LightStates.ON : LightStates.OFF));

						if (!message.isOnOffCommand()) {
							LightService.getInstance().dimLight(hueDevice.getExternalId(), message.getBrightness());
						}

						break;
					case SWITCH:
						ActorService.getInstance().pressSwitch(Long.toString(hueDevice.getExternalId()),
								("on".equals(message.getPayload()) ? "ON" : "OFF"));
						break;
					case WINDOWBLIND:
						String dimValue = "99";
						if (!message.isOnOffCommand()) {
							dimValue = Integer.toString(message.getBrightness());
						}
						System.out.println("Window Blind dim: " + dimValue);

						new WindowBlindService().setDim(hueDevice.getExternalId(),
								("on".equals(message.getPayload()) ? dimValue : "0"),
								hueDevice.isGroupDevice() ? WindowBlind.ALL_AT_ONCE : WindowBlind.SINGLE,
								(hueDevice.isGroupDevice() ? hueDevice.getRoom().getId() : null));
						break;
					default:
						break;
					}
				}
			} else {
				System.out.println("hue not found for lightId: " + lightId);
			}
		}

		return new GenericStatus(true);
	}

	public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
		 ObjectMapper mapper = new ObjectMapper();
		 
			String message = "{\"xy\": [0.24224, 0.4444]}";
		 
			HueEmulatorMessage readValue = mapper.readValue(message, HueEmulatorMessage.class);
		 
			System.out.println(readValue.getXy()[1]);
	}
}
