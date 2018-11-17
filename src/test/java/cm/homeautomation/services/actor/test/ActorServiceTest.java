package cm.homeautomation.services.actor.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.services.actor.ActorService;
import cm.homeautomation.services.actor.SwitchStatuses;

public class ActorServiceTest {

	private ActorService actorService;
	private EntityManager em;

	@BeforeEach
	public void setup() {
		actorService = new ActorService();
		em = EntityManagerService.getNewManager();

	}

	@AfterEach
	public void cleanup() {
		em.getTransaction().begin();

		em.createQuery("delete from Switch").executeUpdate();
		em.getTransaction().commit();

		em.getTransaction().begin();
		em.createQuery("delete from Room r where r.roomName like 'Actor Test Room%'").executeUpdate();

		em.getTransaction().commit();

	}

	@Test
	public void testCronPressSwitch() throws Exception {

	}

	@Test
	public void testGetInstance() throws Exception {

	}

	@Test
	public void testSetInstance() throws Exception {

	}

	@Test
	public void testConnectionLost() throws Exception {

	}

	@Test
	public void testDeliveryComplete() throws Exception {

	}

	@Test
	public void testGetSwitchStatusesForRoom() throws Exception {

	}

	@Test
	public void testGetThermostatStatusesForRoomEmpty() throws Exception {
		SwitchStatuses thermostatStatusesForRoom = actorService.getThermostatStatusesForRoom("1000");

		assertNotNull(thermostatStatusesForRoom);
		assertNotNull(thermostatStatusesForRoom.getSwitchStatuses());
		assertTrue(thermostatStatusesForRoom.getSwitchStatuses().isEmpty());
	}

	@Test
	public void testGetThermostatStatusesForRoomRoomAndThermostatAvailable() throws Exception {

		em.getTransaction().begin();

		Room room = new Room();
		room.setRoomName("Actor Test Room " + System.currentTimeMillis());

		em.persist(room);

		room = em.createQuery("select r from Room r where r.roomName=:roomName", Room.class)
				.setParameter("roomName", room.getRoomName()).getSingleResult();

		Switch thermostatSwitch = new Switch();
		thermostatSwitch.setSwitchType("THERMOSTAT");
		thermostatSwitch.setRoom(room);
		em.persist(thermostatSwitch);

		em.getTransaction().commit();

		SwitchStatuses thermostatStatusesForRoom = actorService.getThermostatStatusesForRoom("" + room.getId());

		assertNotNull(thermostatStatusesForRoom);
		assertNotNull(thermostatStatusesForRoom.getSwitchStatuses());
		assertFalse(thermostatStatusesForRoom.getSwitchStatuses().isEmpty());
	}

	/**
	 * do nothing
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageArrived() throws Exception {
		actorService.messageArrived(null, null);
	}

	@Test
	public void testPressSwitchStringString() throws Exception {

	}

	@Test
	public void testPressSwitchStringArray() throws Exception {

	}

	@Test
	public void testUpdateBackendSwitchState() throws Exception {
	}

}
