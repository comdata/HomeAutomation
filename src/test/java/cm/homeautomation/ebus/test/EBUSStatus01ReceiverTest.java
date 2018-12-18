package cm.homeautomation.ebus.test;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.ebus.EBUSStatus01Receiver;
import cm.homeautomation.ebus.EBusMessageEvent;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.sensors.Sensors;
import mockit.Expectations;

class EBUSStatus01ReceiverTest {

	@Test
	public void testReceiverRegistered() {
		EBUSStatus01Receiver ebusStatus01Receiver = new EBUSStatus01Receiver();

		boolean containsKey = EventBusService.getEventBus().getClasses()
				.containsKey("cm.homeautomation.ebus.EBUSStatus01Receiver");
		Assertions.assertTrue(containsKey);

		EventBusService.getEventBus().unregister(ebusStatus01Receiver);
	}

	@Test
	public void testReceiveStatus01NoResultException() {
		new Sensors();
		String messageContent = "37.0;36.0;2.250;36.0;39.0;off";
		String topic = "ebusd/bai/Status01";

		EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);

		EBUSStatus01Receiver ebusStatus01Receiver = new EBUSStatus01Receiver();
		boolean containsKey = EventBusService.getEventBus().getClasses()
				.containsKey("cm.homeautomation.ebus.EBUSStatus01Receiver");
		Assertions.assertTrue(containsKey);

		new Expectations() {
			{
				// logger.debug(message);
			}
		};
		Assertions.assertThrows(NoResultException.class, () -> {
			ebusStatus01Receiver.receive(new EventObject(ebusMessageEvent));
		});
	}

	@Test
	public void testReceiveStatus01() {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		Sensor sensor = new Sensor();
		sensor.setSensorName("OUTSIDETEMP");
		sensor.setSensorTechnicalType("OUTSIDETEMP");
		em.persist(sensor);
		em.getTransaction().commit();
		
		new Sensors();
		String messageContent = "37.0;36.0;2.250;36.0;39.0;off";
		String topic = "ebusd/bai/Status01";

		EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);

		EBUSStatus01Receiver ebusStatus01Receiver = new EBUSStatus01Receiver();
		boolean containsKey = EventBusService.getEventBus().getClasses()
				.containsKey("cm.homeautomation.ebus.EBUSStatus01Receiver");
		Assertions.assertTrue(containsKey);

		new Expectations() {
			{
				// logger.debug(message);
			}
		};

		ebusStatus01Receiver.receive(new EventObject(ebusMessageEvent));
		
		em.getTransaction().begin();
		
		em.createQuery("delete from SensorData").executeUpdate();
		em.createQuery("delete from Sensor").executeUpdate();
		
		em.getTransaction().commit();
		
		em.close();
	}

	@Test
	public void testReceiveEmptyEventObject() {
		EBUSStatus01Receiver ebusStatus01Receiver = new EBUSStatus01Receiver();
		boolean containsKey = EventBusService.getEventBus().getClasses()
				.containsKey("cm.homeautomation.ebus.EBUSStatus01Receiver");
		Assertions.assertTrue(containsKey);
		new Expectations() {
			{
				LogManager.getLogger(EBUSStatus01Receiver.class).error("Empty event received.");
			}
		};

		ebusStatus01Receiver.receive(new EventObject(null));
	}

}
