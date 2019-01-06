package cm.homeautomation.services.sensors.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.sensors.RFEvent;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;

public class SensorsTest {

	private static final String RF_SWITCH_ROOM = "rf switch room";
	private Sensors sensors;
	private EntityManager em;
	private Room room;

	@BeforeEach
	public void setup() {
		sensors = new Sensors();
		em = EntityManagerService.getNewManager();

		em.getTransaction().begin();

		em.createQuery("delete from SensorData").executeUpdate();
		em.createQuery("delete from Sensor").executeUpdate();
		em.createQuery("delete from Switch").executeUpdate();
		em.createQuery("delete from Room r where r.roomName=:roomName").setParameter("roomName", RF_SWITCH_ROOM)
				.executeUpdate();

		room = new Room();
		room.setRoomName(RF_SWITCH_ROOM);

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

	@Test
	public void testMaxLimitCheck() {
		em.getTransaction().begin();

		Sensor sensor = new Sensor();
		sensor.setMaxValue("100");
		sensor.setSensorName("Test Sensor");
		sensor.setRoom(room);
		String sensorTechnicalType = "TESTSENSOR";
		sensor.setSensorTechnicalType(sensorTechnicalType);

		em.persist(sensor);

		em.getTransaction().commit();

		SensorDataSaveRequest saveRequest = new SensorDataSaveRequest();
		SensorData sensorData = new SensorData();
		sensorData.setSensor(sensor);
		sensorData.setValue("101");
		sensorData.setDateTime(new Date());
		saveRequest.setSensorData(sensorData);

		Assertions.assertThrows(SensorDataLimitViolationException.class, () -> {
			sensors.saveSensorData(saveRequest);
		});
	}
}
