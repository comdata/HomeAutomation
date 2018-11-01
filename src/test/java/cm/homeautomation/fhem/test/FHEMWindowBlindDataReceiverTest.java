package cm.homeautomation.fhem.test;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.FHEMDevice.FHEMDeviceType;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.fhem.FHEMWindowBlindDataReceiver;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.sensors.Sensors;

public class FHEMWindowBlindDataReceiverTest {
	private EntityManager em;
	private Room room;
	
	@Before
	public void setup() {
		em = EntityManagerService.getNewManager();

		em.getTransaction().begin();
		room = new Room();
		room.setRoomName("FHEM Room "+System.currentTimeMillis());

		em.persist(room);

		em.getTransaction().commit();
	}
	
	@Test
	public void testReceiveSuccessful() {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		WindowBlind windowBlind = new WindowBlind();
		windowBlind.setName(""+System.currentTimeMillis());
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		
		FHEMDevice fhemDevice = new FHEMDevice();
		fhemDevice.setDeviceType(FHEMDeviceType.WINDOWBLIND);
		fhemDevice.setReferencedId(windowBlind.getId());
		fhemDevice.setName(windowBlind.getName());
		em.persist(fhemDevice);
		
		
		em.getTransaction().commit();
		
		GenericStatus genericStatus = FHEMWindowBlindDataReceiver.receive("/fhem/"+windowBlind.getName()+"/state","dim 95", fhemDevice);
	
		assertNotNull(genericStatus);
		assertTrue(genericStatus.isSuccess());
	}

	@Test
	public void testReceiveGeneratesNull() {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		WindowBlind windowBlind = new WindowBlind();
		windowBlind.setName(""+System.currentTimeMillis());
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		
		FHEMDevice fhemDevice = new FHEMDevice();
		fhemDevice.setDeviceType(FHEMDeviceType.WINDOWBLIND);
		fhemDevice.setReferencedId(windowBlind.getId());
		fhemDevice.setName(windowBlind.getName());
		em.persist(fhemDevice);
		
		em.getTransaction().commit();
		
		GenericStatus genericStatus = FHEMWindowBlindDataReceiver.receive("/fhem/"+windowBlind.getName()+"/nostate","dim 95", fhemDevice);
	
		assertNull(genericStatus);
	}
}
