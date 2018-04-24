package cm.homeautomation.services.window.test;

import javax.persistence.EntityManager;

import org.junit.Before;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Window;
import cm.homeautomation.services.window.WindowStateService;

public class WindowStateServiceTest {

	private WindowStateService windowStateService;
	private EntityManager em;
	private Window window;

	@Before
	public void setup() {
		windowStateService = new WindowStateService();
		em = EntityManagerService.getNewManager();

		em.getTransaction().begin();

		final Room room = new Room();
		room.setRoomName("Test Window Room " + Math.random());
		room.setVisible(true);

		em.persist(room);

		window = new Window();

		window.setName("Test Window " + Math.random());
		window.setRoom(room);

		em.persist(window);

		em.getTransaction().commit();

	}

	// @Test
	// public void testWindowSensorCreate() throws Exception {
	// assertNotNull(window.getId());
	//
	// windowStateService.handleWindowState(window.getId(), "open");
	//
	// em = EntityManagerService.getNewManager();
	//
	// final List<Window> resultList = em.createQuery("select w from Window w where
	// w.id=:id")
	// .setParameter("id", window.getId()).getResultList();
	//
	// for (final Window window : resultList) {
	// assertNotNull(window.getStateSensor());
	// }
	// }
	//
	// @Test
	// public void testWindowStateClosed() throws Exception {
	//
	// windowStateService.handleWindowState(window.getId(), "closed");
	//
	// final List<WindowStateData> list = windowStateService.get();
	// for (final WindowStateData windowStateData : list) {
	//
	// if (windowStateData.getWindow().equals(window)) {
	// assertTrue(windowStateData.getState() == 0);
	// }
	// }
	//
	// }
	//
	// @Test
	// public void testWindowStateOpen() throws Exception {
	//
	// windowStateService.handleWindowState(window.getId(), "open");
	//
	// final List<WindowStateData> list = windowStateService.get();
	// for (final WindowStateData windowStateData : list) {
	// if (windowStateData.getWindow().equals(window)) {
	// assertTrue(windowStateData.getState() == 1);
	// }
	// }
	//
	// }

}
