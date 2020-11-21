package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowBlind;
import de.a9d3.testing.checks.GetterIsSetterCheck;
import de.a9d3.testing.checks.PublicVariableCheck;
import de.a9d3.testing.executer.SingleThreadExecutor;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class WindowBlindTest {
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	private String calibrationUrl = "http://www.google.de";
	private WindowBlind windowBlind;
	private Room room;

	@BeforeEach
	public void setup() {

		
		room = new Room();
		room.setRoomName("Test Sensor Room");

		em.persist(room);
		room = em.merge(room);
		
	}

	@AfterEach
	public void tearDown() {

		if (windowBlind != null) {
			
			
			windowBlind = em.merge(windowBlind);
			em.remove(windowBlind);
			
		}
	}

	@Test
	public void testCreateBlind() throws Exception {
		
		
		windowBlind = new WindowBlind();

		windowBlind.setName("Testblind");
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		
	}

	@Test
	public void testWindowBlindFailsWithoutRoom() throws Exception {

		Assertions.assertThrows(RollbackException.class, () -> {
			
			
			windowBlind = new WindowBlind();

			windowBlind.setName("Testblind");
			em.persist(windowBlind);
			
		});
	}

	@Test
	public void testCalibrationURL() throws Exception {
		
		
		windowBlind = new WindowBlind();

		windowBlind.setCalibrationUrl(calibrationUrl);
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		

		assertTrue(windowBlind.getId() != null, "id is null");
		assertTrue(windowBlind.getId().longValue() > 0, "id is not greater 0");
		assertTrue(calibrationUrl.equals(windowBlind.getCalibrationUrl()), "calibration Url is not: " + calibrationUrl);
	}

	@Test
	public void baseTest() {
		SingleThreadExecutor executor = new SingleThreadExecutor();

		assertTrue(executor.execute(WindowBlind.class, Arrays.asList(
				// new CopyConstructorCheck(),
				// new DefensiveCopyingCheck(),
				// new EmptyCollectionCheck(),
				new GetterIsSetterCheck(),
				// new HashcodeAndEqualsCheck(),
				new PublicVariableCheck(true))));
	}
}
