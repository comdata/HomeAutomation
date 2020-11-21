package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Room;

import de.a9d3.testing.checks.*;
import de.a9d3.testing.executer.SingleThreadExecutor;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoomTest {

	@Inject
	EntityManager em;
	
	@Inject
	ConfigurationService configurationService;

	@BeforeEach
	public void setup() {
	

	}
	
	@Test
	public void testCreateRoom() throws Exception {
		
		Room room=new Room();
		room.setRoomName("Test Room");

		em.persist(room);
		
		
		assertNotNull(room);
    }
    
    @Test
    public void baseTest() {
        SingleThreadExecutor executor = new SingleThreadExecutor();

        assertTrue(executor.execute(Room.class, Arrays.asList( 
                //new CopyConstructorCheck(), 
                //new DefensiveCopyingCheck(),
                //new EmptyCollectionCheck(), 
                new GetterIsSetterCheck(),
                //new HashcodeAndEqualsCheck(), 
                new PublicVariableCheck(true))));
    }
}
