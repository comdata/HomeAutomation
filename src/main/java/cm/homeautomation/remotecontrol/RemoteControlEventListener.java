package cm.homeautomation.remotecontrol;

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

@AutoCreateInstance
public class RemoteControlEventListener {
	public RemoteControlEventListener() {
		EventBusService.getEventBus().register(this);
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

		for (RemoteControl remoteControl : remoteList) {

			if (remoteControl != null) {
				List<RemoteControlGroup> remoteControlGroups = em
						.createQuery("select cg from RemoteControlGroup cg where cg.remote=:remote",
								RemoteControlGroup.class)
						.setParameter("remote", remoteControl).getResultList();
				for (RemoteControlGroup remoteControlGroup : remoteControlGroups) {
					List<RemoteControlGroupMember> members = remoteControlGroup.getMembers();

					for (RemoteControlGroupMember remoteControlGroupMember : members) {
						LogManager.getLogger(this.getClass()).error("found remote member: " + remoteControl.getName());
						switch (remoteControlGroupMember.getType()) {
						case LIGHT:
							LightService.getInstance().setLightState(remoteControlGroupMember.getExternalId(),
									(event.isPoweredOnState() ? LightStates.ON : LightStates.OFF));
							break;
						case SWITCH:
							ActorService.getInstance().pressSwitch(Long.toString(remoteControlGroupMember
									.getExternalId()),
									(event.isPoweredOnState() ? "ON" : "OFF"));
							break;
						case WINDOWBLIND:
							new WindowBlindService().setDim(remoteControlGroupMember.getExternalId(),
									(event.isPoweredOnState() ? "99" : "0"));
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
	}
}
