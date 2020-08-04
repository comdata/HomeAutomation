package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowBlind;

import de.a9d3.testing.checks.*;
import de.a9d3.testing.executer.SingleThreadExecutor;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WindowBlindTest {
	private EntityManager em;
	private String calibrationUrl="http://www.google.de";
	private WindowBlind windowBlind;
	private Room room;
	
	@BeforeEach
	public void setup() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		room = new Room();
		room.setRoomName("Test Sensor Room");

		em.persist(room);
		room=em.merge(room);
		em.getTransaction().commit();
	}
	
	@AfterEach
	public void tearDown() {
                
        if (windowBlind!=null) {
            em = EntityManagerService.getNewManager();
		    em.getTransaction().begin();
		    windowBlind=em.merge(windowBlind);
		    em.remove(windowBlind);
            em.getTransaction().commit();
        }
	}
	
	@Test
	public void testCreateBlind() throws Exception {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		windowBlind = new WindowBlind();
		
		windowBlind.setName("Testblind");
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		em.getTransaction().commit();
	}
	
	@Test
	public void testWindowBlindFailsWithoutRoom() throws Exception {

        Assertions.assertThrows(RollbackException.class, () -> {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		windowBlind = new WindowBlind();

		windowBlind.setName("Testblind");
		em.persist(windowBlind);
        em.getTransaction().commit();		
    });
	}
	
	@Test
	public void testCalibrationURL() throws Exception {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		windowBlind = new WindowBlind();
		
		windowBlind.setCalibrationUrl(calibrationUrl);
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		em.getTransaction().commit();
		
		assertTrue(windowBlind.getId()!=null, "id is null");
		assertTrue(windowBlind.getId().longValue()>0, "id is not greater 0");
		assertTrue(calibrationUrl.equals(windowBlind.getCalibrationUrl()), "calibration Url is not: " + calibrationUrl);
    }
    

    @Test
    public void baseTest() {
        SingleThreadExecutor executor = new SingleThreadExecutor();

        assertTrue(executor.execute(WindowBlind.class, Arrays.asList( 
                //new CopyConstructorCheck(), 
                //new DefensiveCopyingCheck(),
                //new EmptyCollectionCheck(), 
                new GetterIsSetterCheck(),
                //new HashcodeAndEqualsCheck(), 
                new PublicVariableCheck(true))));
    }
}
