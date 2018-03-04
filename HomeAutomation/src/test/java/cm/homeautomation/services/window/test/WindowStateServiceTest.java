package cm.homeautomation.services.window.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Window;
import cm.homeautomation.sensors.window.WindowStateData;
import cm.homeautomation.services.window.WindowStateService;

public class WindowStateServiceTest {

	private WindowStateService windowStateService;
	private EntityManager em;
	private Window window;

	@Before
	private void setup() {
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

	@Test
	public void testWindowSensorCreate() throws Exception {

		windowStateService.handleWindowState(window.getId(), "open");

		assertNotNull(window.getStateSensor());
	}

	@Test
	public void testWindowStateClosed() throws Exception {

		windowStateService.handleWindowState(window.getId(), "closed");

		final List<WindowStateData> list = windowStateService.get();
		for (final WindowStateData windowStateData : list) {

			if (windowStateData.getWindow().equals(window)) {
				assertTrue(windowStateData.getState() == 0);
			}
		}

	}

	@Test
	public void testWindowStateOpen() throws Exception {

		windowStateService.handleWindowState(window.getId(), "open");

		final List<WindowStateData> list = windowStateService.get();
		for (final WindowStateData windowStateData : list) {
			if (windowStateData.getWindow().equals(window)) {
				assertTrue(windowStateData.getState() == 1);
			}
		}

	}

}
