package cm.homeautomation.remotecontrol;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.entities.RemoteControl;
import cm.homeautomation.entities.RemoteControlGroup;
import cm.homeautomation.entities.RemoteControlGroupMember;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.services.actor.ActorService;
import cm.homeautomation.services.light.LightService;
import cm.homeautomation.services.light.LightStates;
import cm.homeautomation.services.networkmonitor.NetworkWakeupEvent;
import cm.homeautomation.services.windowblind.WindowBlindDimMessageSimple;
import cm.homeautomation.services.windowblind.WindowBlindService;
import cm.homeautomation.services.windowblind.WindowBlindService.DimDirection;
import cm.homeautomation.zigbee.RemoteControlBrightnessChangeEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Startup
@ApplicationScoped
@Transactional(value = TxType.REQUIRES_NEW)
public class RemoteControlEventListener {

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@ConsumeEvent(value = "RemoteControlBrightnessChangeEvent", blocking = true)
	public void subscribe(RemoteControlBrightnessChangeEvent event) {
		String name = event.getName();
		String technicalId = event.getTechnicalId();

		LogManager.getLogger(this.getClass()).debug("got remote event: " + name + "/" + technicalId);

		List<RemoteControl> remoteList = em
				.createQuery("select rc from RemoteControl rc where rc.technicalId=:technicalId", RemoteControl.class)
				.setParameter("technicalId", technicalId).getResultList();

		if (remoteList != null && !remoteList.isEmpty()) {
			for (RemoteControl remoteControl : remoteList) {

				if (remoteControl != null) {
					List<RemoteControlGroup> remoteControlGroups = remoteControl.getGroups();

					for (RemoteControlGroup remoteControlGroup : remoteControlGroups) {
						List<RemoteControlGroupMember> members = remoteControlGroup.getMembers();

						LogManager.getLogger(this.getClass())
								.debug("found remote group: " + remoteControlGroup.getName());

						for (RemoteControlGroupMember remoteControlGroupMember : members) {

							final Runnable controlMemberThread = () -> {
								LogManager.getLogger(this.getClass())
										.debug("found remote member: " + remoteControl.getName());
								switch (remoteControlGroupMember.getType()) {
								case LIGHT:
									LightService.getInstance().dimLight(remoteControlGroupMember.getExternalId(),
											event.getBrightness());
									break;
								case SWITCH:
									break;
								case WINDOWBLIND:
									break;
								case NETWORKDEVICE:
									break;
								case SCRIPT:
									break;
								default:
									break;
								}
							};

							new Thread(controlMemberThread).start();
						}
					}
				}
			}
		}
	}

	@ConsumeEvent(value = "RemoteControlEvent", blocking = true)
	
	public void subscribe(RemoteControlEvent event) {
		String name = event.getName();
		String technicalId = event.getTechnicalId();

		System.out.println("RC " + name + " " + technicalId);

		// LogManager.getLogger(this.getClass()).error("got remote event: " + name + "/"
		// + technicalId);

		List<RemoteControl> remoteList = em
				.createQuery("select rc from RemoteControl rc where rc.technicalId=:technicalId", RemoteControl.class)
				.setParameter("technicalId", technicalId).getResultList();

		if (remoteList != null && !remoteList.isEmpty()) {
			for (RemoteControl remoteControl : remoteList) {

				if (remoteControl != null) {
					List<RemoteControlGroup> remoteControlGroups = remoteControl.getGroups();

					for (RemoteControlGroup remoteControlGroup : remoteControlGroups) {
						List<RemoteControlGroupMember> members = remoteControlGroup.getMembers();

						// LogManager.getLogger(this.getClass())
						// .debug("found remote group: " + remoteControlGroup.getName());

						for (RemoteControlGroupMember remoteControlGroupMember : members) {
//                            LogManager.getLogger(this.getClass())
//                                    .debug("found remote member: " + remoteControl.getName());
							switch (remoteControlGroupMember.getType()) {
							case LIGHT:
								LightService.getInstance().setLightState(remoteControlGroupMember.getExternalId(),
										(event.isPoweredOnState() ? LightStates.ON : LightStates.OFF), false);
								break;
							case SWITCH:
								ActorService.getInstance().pressSwitch(
										Long.toString(remoteControlGroupMember.getExternalId()),
										(event.isPoweredOnState() ? "ON" : "OFF"));
								break;
							case WINDOWBLIND:

								switch (event.getEventType()) {

								case ON_OFF:

									DimDirection dimDirection;
									if (event.getClick().equals("open")) {
										dimDirection = DimDirection.UP;
									} else {
										dimDirection = DimDirection.DOWN;
									}

									new WindowBlindService().dim(dimDirection,
											remoteControlGroupMember.getExternalId());

									break;

								case REMOTE:
									WindowBlindDimMessageSimple windowBlindDimMessage = new WindowBlindDimMessageSimple(
											remoteControlGroupMember.getExternalId(),
											(event.isPoweredOnState() ? "99" : "0"));
									bus.publish("WindowBlindDimMessageSimple", windowBlindDimMessage);
									break;
								}

								break;
							case NETWORKDEVICE:

								if (event.isPoweredOnState()) {
									NetworkDevice networkDevice = em.find(NetworkDevice.class,
											remoteControlGroupMember.getExternalId());

									if (networkDevice != null) {
										bus.publish("NetworkWakeUpEvent",
												new NetworkWakeupEvent(networkDevice.getMac()));

									}
								}
								break;
							default:
								break;

							}

						}
					}

				} else {
					// control not found
				}
			}
		} else {
			// remote not found let's create it

			RemoteControl remoteControl = new RemoteControl();

			remoteControl.setTechnicalId(technicalId);
			remoteControl.setName(name);
			remoteControl.setRemoteType(event.getRemoteType());
			List<RemoteControlGroup> groups = new ArrayList<>();
			RemoteControlGroup group = new RemoteControlGroup();
			group.setName(name);
			group.setRemote(remoteControl);
			groups.add(group);
			remoteControl.setGroups(groups);
			em.persist(remoteControl);
			em.persist(group);

		}
	}
}
