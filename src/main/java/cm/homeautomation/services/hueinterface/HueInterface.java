package cm.homeautomation.services.hueinterface;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.HueDevice;
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

		List<HueDevice> hueDeviceList = em.createQuery("select hd from HueDevice hd where hd.lightId=:lightId",
				HueDevice.class)
				.setParameter("lightId", message.getLightId()).getResultList();

		if (hueDeviceList == null || hueDeviceList.isEmpty()) {
			em.getTransaction().begin();

			HueDevice hueDevice = new HueDevice();
			hueDevice.setName(message.getDeviceName());
			hueDevice.setLightId(message.getLightId());
			em.persist(hueDevice);
			em.getTransaction().commit();
		} else {
			HueDevice hueDevice = hueDeviceList.get(0);

			switch (hueDevice.getType()) {
			case LIGHT:
				LightService.getInstance().setLightState(hueDevice
						.getExternalId(),
						("on".equals(message.getPayload()) ? LightStates.ON : LightStates.OFF));

				if (!message.isOnOffCommand()) {
					LightService.getInstance().dimLight(hueDevice
							.getExternalId(), message.getBrightness());
				}

				break;
			case SWITCH:
				ActorService.getInstance().pressSwitch(Long.toString(hueDevice
						.getExternalId()),
						("on".equals(message.getPayload()) ? "ON" : "OFF"));
				break;
			case WINDOWBLIND:
				String dimValue = "99";
				if (!message.isOnOffCommand()) {
					dimValue = Integer.toString(message.getBrightness());
				}
				
				new WindowBlindService()
						.setDim(hueDevice.getExternalId(),
						("on".equals(message.getPayload()) ? dimValue : "0"));
				break;
			default:
				break;
			}
		}

		return null;
	}
}
