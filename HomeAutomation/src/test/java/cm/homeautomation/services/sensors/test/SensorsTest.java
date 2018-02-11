package cm.homeautomation.services.sensors.test;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;

import org.apache.catalina.deploy.SessionConfig;
import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.sensors.RFEvent;
import cm.homeautomation.services.sensors.Sensors;

public class SensorsTest {

	private Sensors sensors;
	private EntityManager em;
	private Room room;

	@Before
	public void setup() {
		sensors = new Sensors();
		em = EntityManagerService.getNewManager();

		em.getTransaction().begin();
		room = new Room();
		room.setRoomName("rf switch room");

		em.persist(room);

		em.getTransaction().commit();
	}

	@Test
	public void testRFEvent() throws Exception {
		int onCode = 1111;
		Switch rfSwitch = new Switch();
		rfSwitch.setOnCode("" + onCode);
		rfSwitch.setRoom(room);
		rfSwitch.setName("Test switch");

		em.getTransaction().begin();
		em.persist(rfSwitch);
		em.getTransaction().commit();

		RFEvent rfEvent = new RFEvent();
		rfEvent.setCode(onCode);
		sensors.registerRFEvent(rfEvent);
	}

	@Test
	public void testSensorDifferenceNotBigEnough() {

		try {
			SensorData existingSensorData = new SensorData();
			SensorData requestSensorData = new SensorData();

			existingSensorData.setValue("20");
			existingSensorData.setValue("21");

			boolean mergeExistingData = sensors.mergeExistingData(existingSensorData, requestSensorData);

			assertTrue("merging possible", mergeExistingData);
		} catch (Exception e) {

		}

	}
}
