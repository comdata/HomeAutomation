package cm.homeautomation.services.hueinterface;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jboss.logging.Logger;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.HueDevice;
import cm.homeautomation.entities.HueDeviceType;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.RemoteControl;
import cm.homeautomation.entities.RemoteControl.RemoteType;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.events.RemoteControlEvent.EventType;
import cm.homeautomation.services.actor.ActorPressSwitchEvent;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.light.LightService;
import cm.homeautomation.services.light.LightStates;
import cm.homeautomation.services.windowblind.WindowBlindDimMessage;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
@Path("hueInterface")
public class HueInterface extends BaseService {

	@Inject
	LightService lightService;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Inject
	EventBus bus;

	private static final Logger LOG = Logger.getLogger(HueInterface.class);

	@ConsumeEvent(value = "HueEmulatorMessage", blocking = true)
	public void handleEventBusMessage(HueEmulatorMessage message) {
		handleMessage(message);
	}

	@POST
	@Path("send")
	@Transactional
	public GenericStatus handleMessage(HueEmulatorMessage message) {

		String lightId = message.getLightId();

		if (lightId != null) {

			List<HueDevice> hueDeviceList = em
					.createQuery("select hd from HueDevice hd where hd.lightId=:lightId", HueDevice.class)
					.setParameter("lightId", lightId).getResultList();

			if (hueDeviceList == null || hueDeviceList.isEmpty()) {

				List<HueDevice> hueDeviceNameList = em
						.createQuery("select hd from HueDevice hd where hd.name=:name", HueDevice.class)
						.setParameter("name", message.getDeviceName())
						.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS).getResultList();
				if (hueDeviceNameList != null && !hueDeviceNameList.isEmpty()) {
					HueDevice hueDevice = hueDeviceNameList.get(0);

					hueDevice.setLightId(message.getLightId());
					em.merge(hueDevice);

					handleMessage(message);
				} else {

					// try to find existing device
					long externalId = 0;
					HueDeviceType type = null;

					List<Switch> switchList = em
							.createQuery("select sw from Switch sw where sw.name=:name", Switch.class)
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
						} else {
							List<WindowBlind> windowBlindList = em
									.createQuery("select w from WindowBlind w where w.name=:name", WindowBlind.class)
									.setParameter("name", message.getDeviceName()).getResultList();

							if (windowBlindList != null && !windowBlindList.isEmpty()) {
								WindowBlind windowBlind = windowBlindList.get(0);
								externalId = windowBlind.getId();
								type = HueDeviceType.WINDOWBLIND;
							} else {
								List<RemoteControl> remoteList = em
										.createQuery("select r from RemoteControl r where r.name=:name",
												RemoteControl.class)
										.setParameter("name", message.getDeviceName()).getResultList();

								if (remoteList != null && !remoteList.isEmpty()) {
									externalId = remoteList.get(0).getId();
									type = HueDeviceType.REMOTE;
								}
							}
						}
					}

					HueDevice hueDevice = new HueDevice();
					hueDevice.setName(message.getDeviceName());
					hueDevice.setLightId(message.getLightId());

					if (externalId > 0 && type != null) {
						hueDevice.setExternalId(externalId);
						hueDevice.setType(type);

					}

					em.persist(hueDevice);

					if (externalId > 0 && type != null) {
						// do it again, since device is now created
						handleMessage(message);
					}
				}
			} else {
				HueDevice hueDevice = hueDeviceList.get(0);

				if (hueDevice != null) {

					HueDeviceType type = hueDevice.getType();
					if (type != null) {
						switch (type) {
						case LIGHT:

							handleLight(message, hueDevice);

							break;
						case SWITCH:
							handleSwitch(message, hueDevice);
							break;
						case WINDOWBLIND:
							handleWindowBlind(message, hueDevice);
							break;
						case REMOTE:
							handleRemote(message, hueDevice);
							break;
						default:
							break;
						}
					}
				} else {
					LOG.debug("hue not found for lightId: " + lightId);
				}
			}

			return new GenericStatus(true);
		} else {
			return new GenericStatus(true, "Light Id is null");
		}
	}

	private void handleRemote(HueEmulatorMessage message, HueDevice hueDevice) {
		RemoteControlEvent remoteControlEvent = new RemoteControlEvent(hueDevice.getName(),
				hueDevice.getId().toString(), EventType.REMOTE, RemoteType.HUE);

		remoteControlEvent.setPoweredOnState("on".equals(message.getPayload()));
		bus.publish("RemoteControlEvent", remoteControlEvent);
	}

	private void handleWindowBlind(HueEmulatorMessage message, HueDevice hueDevice) {

		WindowBlind windowBlind = em.find(WindowBlind.class, hueDevice.getExternalId());

		if (windowBlind != null) {

			String dimValue = Integer.toString(windowBlind.getMaximumValue());
			if (!message.isOnOffCommand()) {
				dimValue = Integer.toString(message.getBrightness());
			}
//            LogManager.getLogger(HueInterface.class).debug("Window Blind dim: " + dimValue);

			WindowBlindDimMessage windowBlindDimMessage = new WindowBlindDimMessage(hueDevice.getExternalId(),
					("on".equals(message.getPayload()) ? dimValue : "0"),
					hueDevice.isGroupDevice() ? WindowBlind.ALL_AT_ONCE : WindowBlind.SINGLE,
					(hueDevice.isGroupDevice() ? hueDevice.getRoom().getId() : null));

			bus.publish("WindowBlindDimMessage", windowBlindDimMessage);

		}
	}

	private void handleSwitch(HueEmulatorMessage message, HueDevice hueDevice) {
		ActorPressSwitchEvent actorPressSwitchEvent = new ActorPressSwitchEvent(
				Long.toString(hueDevice.getExternalId()), ("on".equals(message.getPayload()) ? "ON" : "OFF"));

		bus.publish("ActorPressSwitchEvent", actorPressSwitchEvent);
	}

	private void handleLight(HueEmulatorMessage message, HueDevice hueDevice) {
		if (!message.isOnOffCommand()) {
			lightService.dimLight(hueDevice.getExternalId(), message.getBrightness());
		} else {
			boolean isColor = false;
			Float x = 0f;
			Float y = 0f;
			if (message.getXy() != null) {
				x = message.getXy()[0];
				y = message.getXy()[1];
				LOG.debug("Color infos:" + x + " - " + y);
				isColor = true;
			}

			if (isColor) {
				lightService.setColor(hueDevice.getExternalId(), message.getBrightness(), x, y);
			} else {

				lightService.setLightState(hueDevice.getExternalId(),
						(message.isOn() ? LightStates.ON : LightStates.OFF), false);
			}
		}
	}
}
