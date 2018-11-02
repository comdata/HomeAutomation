package cm.homeautomation.services.sensors.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
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
			Sensor sensor = new Sensor();
			sensor.setDeadbandPercent(1);
			
			existingSensorData.setValue("20");
			requestSensorData.setValue("21");

			boolean mergeExistingData = sensors.mergeExistingData(existingSensorData, requestSensorData);
			System.out.println(mergeExistingData);

			assertTrue("merging possible", mergeExistingData);
		} catch (Exception e) {

		}

	}

	@Test
	public void testSensorDifferenceBigEnough() {

		try {
			SensorData existingSensorData = new SensorData();
			SensorData requestSensorData = new SensorData();

			Sensor sensor = new Sensor();
			sensor.setDeadbandPercent(1);
			existingSensorData.setSensor(sensor);

			existingSensorData.setValue("20");
			requestSensorData.setValue("25");

			boolean mergeExistingData = sensors.mergeExistingData(existingSensorData, requestSensorData);
			System.out.println(mergeExistingData);

			assertFalse("merging not possible", mergeExistingData);
		} catch (Exception e) {

		}

	}
}
