package cm.homeautomation.services.security;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.SecurityZone;
import cm.homeautomation.entities.SecurityZoneMember;
import cm.homeautomation.entities.Window;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.window.WindowStateData;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * Manage Security Services
 *
 * @author christoph
 *
 */
@Path("security/")
@AutoCreateInstance
public class SecurityService extends BaseService {

	private final EntityManager em;

	public SecurityService() {
		em = EntityManagerService.getManager();
		EventBusService.getEventBus().register(this);
	}

	@SuppressWarnings("unchecked")
	@Path("getZones")
	@GET
	public List<SecurityZone> getZones() {

		return em.createQuery("select s from SecurityZone s").getResultList();
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void handleWindowEvents(EventObject eventObject) {
		if ((eventObject.getData() != null) && (eventObject.getData() instanceof WindowStateData)) {
			final WindowStateData windowStateData = (WindowStateData) eventObject.getData();

			final Window window = windowStateData.getWindow();

			final SecurityZoneMember securityZoneMember = (SecurityZoneMember) em
					.createQuery("select szm from SecurityZoneMember szm where szm.window=:window")
					.setParameter("window", window).getSingleResult();

			final SecurityZone securityZone = securityZoneMember.getSecurityZone();

//			LogManager.getLogger(this.getClass())
//					.debug("Security Zone: " + securityZone.getName() + " state: " + securityZone.isState());

			em.getTransaction().begin();

			securityZoneMember.setViolated((windowStateData.getState() == 1) ? true : false);

			em.merge(securityZoneMember);

			em.getTransaction().commit();

			if (securityZone.isState()) {
				if (windowStateData.getState() == 1) {

					final Object securityEvent = new EventObject(new SecurityAlarmEvent(securityZone, window));
					EventBusService.getEventBus().post(securityEvent);
				}

			}
		}

	}

	/**
	 * set the state of a security state
	 *
	 * @param id
	 * @param state
	 *            boolean true=active, false=inactive
	 * @return
	 */
	@GET
	@Path("setZoneState/{id}/{state}")
	public GenericStatus setZoneState(@PathParam("id") Long id, @PathParam("state") boolean state) {
		final SecurityZone securityZone = em.find(SecurityZone.class, id);

		em.getTransaction().begin();

		securityZone.setState(state);

		em.persist(securityZone);

		em.getTransaction().commit();

		return new GenericStatus(true);
	}

}
