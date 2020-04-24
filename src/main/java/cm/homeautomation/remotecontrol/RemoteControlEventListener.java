package cm.homeautomation.remotecontrol;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.RemoteControl;
import cm.homeautomation.entities.RemoteControlGroup;
import cm.homeautomation.entities.RemoteControlGroupMember;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.services.actor.ActorService;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.light.LightService;
import cm.homeautomation.services.light.LightStates;
import cm.homeautomation.services.windowblind.WindowBlindService;
import cm.homeautomation.zigbee.RemoteControlBrightnessChangeEvent;

@AutoCreateInstance
public class RemoteControlEventListener {
	public RemoteControlEventListener() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void subscribe(RemoteControlBrightnessChangeEvent event) {
		String name = event.getName();
		String technicalId = event.getTechnicalId();
		EntityManager em = EntityManagerService.getManager();

		LogManager.getLogger(this.getClass()).error("got remote event: " + name + "/" + technicalId);

		List<RemoteControl> remoteList = em
				.createQuery("select rc from RemoteControl rc where rc.technicalId=:technicalId",
						RemoteControl.class)
				.setParameter("technicalId", technicalId).getResultList();

		if (remoteList != null && !remoteList.isEmpty()) {
			for (RemoteControl remoteControl : remoteList) {

				if (remoteControl != null) {
					List<RemoteControlGroup> remoteControlGroups = remoteControl.getGroups();

					for (RemoteControlGroup remoteControlGroup : remoteControlGroups) {
						List<RemoteControlGroupMember> members = remoteControlGroup.getMembers();

						LogManager.getLogger(this.getClass())
								.error("found remote group: " + remoteControlGroup.getName());

						for (RemoteControlGroupMember remoteControlGroupMember : members) {

							final Runnable controlMemberThread = () -> {
								LogManager.getLogger(this.getClass())
										.error("found remote member: " + remoteControl.getName());
								switch (remoteControlGroupMember.getType()) {
								case LIGHT:
									LightService.getInstance().dimLight(remoteControlGroupMember.getExternalId(),
											event.getBrightness());

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

	@Subscribe
	public void subscribe(RemoteControlEvent event) {
		String name = event.getName();
		String technicalId = event.getTechnicalId();
		EntityManager em = EntityManagerService.getManager();

		LogManager.getLogger(this.getClass()).error("got remote event: " + name + "/" + technicalId);

		List<RemoteControl> remoteList = em
				.createQuery("select rc from RemoteControl rc where rc.technicalId=:technicalId",
						RemoteControl.class)
				.setParameter("technicalId", technicalId).getResultList();

		if (remoteList != null && !remoteList.isEmpty()) {
			for (RemoteControl remoteControl : remoteList) {

				if (remoteControl != null) {
					List<RemoteControlGroup> remoteControlGroups = remoteControl.getGroups();

					for (RemoteControlGroup remoteControlGroup : remoteControlGroups) {
						List<RemoteControlGroupMember> members = remoteControlGroup.getMembers();

						LogManager.getLogger(this.getClass())
								.error("found remote group: " + remoteControlGroup.getName());

						for (RemoteControlGroupMember remoteControlGroupMember : members) {
							final Runnable controlMemberThread = () -> {
								LogManager.getLogger(this.getClass())
										.error("found remote member: " + remoteControl.getName());
								switch (remoteControlGroupMember.getType()) {
								case LIGHT:
									LightService.getInstance().setLightState(remoteControlGroupMember.getExternalId(),
											(event.isPoweredOnState() ? LightStates.ON : LightStates.OFF));
									break;
								case SWITCH:
									ActorService.getInstance().pressSwitch(
											Long.toString(remoteControlGroupMember.getExternalId()),
											(event.isPoweredOnState() ? "ON" : "OFF"));
									break;
								case WINDOWBLIND:
									new WindowBlindService().setDim(remoteControlGroupMember.getExternalId(),
											(event.isPoweredOnState() ? "99" : "0"));
									break;
								default:
									break;

								}
							};

							new Thread(controlMemberThread).start();
						}

					}

				} else {
					// control not found
				}
			}
		} else {
			// remote not found let's create it

			em.getTransaction().begin();

			RemoteControl remoteControl = new RemoteControl();

			remoteControl.setTechnicalId(technicalId);
			remoteControl.setName(name);
			List<RemoteControlGroup> groups = new ArrayList<>();
			RemoteControlGroup group = new RemoteControlGroup();
			group.setName(name);
			group.setRemote(remoteControl);
			groups.add(group);
			remoteControl.setGroups(groups);
			em.persist(remoteControl);
			em.persist(group);
			em.getTransaction().commit();
		}
	}
}
