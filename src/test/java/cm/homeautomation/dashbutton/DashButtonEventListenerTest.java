package cm.homeautomation.dashbutton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

public class DashButtonEventListenerTest {
	private EntityManager em;

	@BeforeEach
	public void setup() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		em.createQuery("delete from DashButton").executeUpdate();
		em.getTransaction().commit();
	}

	@Test
	public void testDashButtonEventListener() throws Exception {
		DashButtonEventListener dashButtonEventListener = new DashButtonEventListener();
		assertNotNull(dashButtonEventListener);
		assertTrue(EventBusService.getEventBus().getClasses().containsValue(DashButtonEventListener.class));
	}

	@Test
	public void testDestroy() throws Exception {
		DashButtonEventListener dashButtonEventListener = new DashButtonEventListener();
		assertNotNull(dashButtonEventListener);
		assertTrue(EventBusService.getEventBus().getClasses().containsValue(DashButtonEventListener.class));
		dashButtonEventListener.destroy();
		assertFalse(EventBusService.getEventBus().getClasses().containsValue(DashButtonEventListener.class));
	}

	@Test
	public void testHandleEvent() throws Exception {
		DashButtonEventListener dashButtonEventListener = new DashButtonEventListener();
		assertNotNull(dashButtonEventListener);
		assertTrue(EventBusService.getEventBus().getClasses().containsValue(DashButtonEventListener.class));

		dashButtonEventListener.handleEvent(new EventObject(new DashButtonEvent("aa:bb:cc:dd:ee:ff")));
		
		
		List<DashButton> resultList = em.createQuery("select d from DashButton d where d.mac=:mac", DashButton.class).setParameter("mac", "AABBCCDDEEFF").getResultList();
		
		assertNotNull(resultList);
		assertFalse(resultList.isEmpty());
		assertNotNull(resultList.get(0));
	}

}
