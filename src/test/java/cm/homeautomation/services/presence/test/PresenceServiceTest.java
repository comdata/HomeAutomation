package cm.homeautomation.services.presence.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Person;
import cm.homeautomation.entities.PresenceState;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.presence.PresenceService;

public class PresenceServiceTest {

	private PresenceService presenceService;
	private Person person;

	@Before
	public void setup() {

		presenceService = new PresenceService();
		presenceService.purgeStates();

		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		person = new Person();
		person.setName("Test User - Presence Service");

		em.persist(person);

		em.getTransaction().commit();
	}

	@Test
	public void testPurging() throws Exception {
		GenericStatus purgeStates = presenceService.purgeStates();

		assertTrue(purgeStates.isSuccess());
	}

	@Test
	public void testGetPresenceNotNull() throws Exception {
		presenceService.purgeStates();
		List<PresenceState> presences = presenceService.getPresences();

		assertTrue(presences != null);
	}

	@Test
	public void testGetPresenceEmpty() throws Exception {
		List<PresenceState> presences = presenceService.getPresences();

		System.out.println(presences);
		assertTrue("presences size: "+ presences.size(), presences.isEmpty());
	}

	@Test
	public void testGetPresenceExisting() throws Exception {
		presenceService.setPresence(person.getId(), "PRESENT");

		List<PresenceState> presences = presenceService.getPresences();

		assertTrue(!presences.isEmpty());
	}
}
