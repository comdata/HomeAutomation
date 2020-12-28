package cm.homeautomation.services.security;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.SecurityZone;
import cm.homeautomation.entities.SecurityZoneMember;
import cm.homeautomation.entities.Window;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.window.WindowStateData;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * Manage Security Services
 *
 * @author christoph
 *
 */
@Path("security/")
@ApplicationScoped
public class SecurityService extends BaseService {

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	public SecurityService() {

	}

	@SuppressWarnings("unchecked")
	@Path("getZones")
	@GET
	public List<SecurityZone> getZones() {

		return em.createQuery("select s from SecurityZone s").getResultList();
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	
	public void handleWindowEvents(EventObject eventObject) {
		if ((eventObject.getData() != null) && (eventObject.getData() instanceof WindowStateData)) {
			final WindowStateData windowStateData = (WindowStateData) eventObject.getData();

			final Window window = windowStateData.getWindow();

			final SecurityZoneMember securityZoneMember = (SecurityZoneMember) em
					.createQuery("select szm from SecurityZoneMember szm where szm.window=:window")
					.setParameter("window", window).getSingleResult();

			final SecurityZone securityZone = securityZoneMember.getSecurityZone();

//			//LogManager.getLogger(this.getClass())
//					.debug("Security Zone: " + securityZone.getName() + " state: " + securityZone.isState());

			securityZoneMember.setViolated((windowStateData.getState() == 1) ? true : false);

			em.merge(securityZoneMember);

			if (securityZone.isState()) {
				if (windowStateData.getState() == 1) {

					final Object securityEvent = new EventObject(new SecurityAlarmEvent(securityZone, window));
					bus.publish("EventObject", securityEvent);
				}

			}
		}

	}

	/**
	 * set the state of a security state
	 *
	 * @param id
	 * @param state boolean true=active, false=inactive
	 * @return
	 */
	@GET
	@Path("setZoneState/{id}/{state}")
	
	public GenericStatus setZoneState(@PathParam("id") Long id, @PathParam("state") boolean state) {
		final SecurityZone securityZone = em.find(SecurityZone.class, id);

		securityZone.setState(state);

		em.persist(securityZone);

		return new GenericStatus(true);
	}

}
