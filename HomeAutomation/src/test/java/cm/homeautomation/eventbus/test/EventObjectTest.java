package cm.homeautomation.eventbus.test;

import static org.junit.Assert.*;

import org.junit.Test;

import cm.homeautomation.eventbus.EventObject;

/**
 * Tests the Event object esp. if the package and class name are set
 * 
 * @author mertins
 *
 */
public class EventObjectTest {
	@Test
	public void testCreateEventObject() throws Exception {
		EventObject eventObject = new EventObject(null);
		assertNotNull(eventObject);
		assertNull(eventObject.getData());
	}
	
	@Test
	public void testCreateEventObjectWithData() throws Exception {
		Object testData = new Object();
		EventObject eventObject = new EventObject(testData);
		assertNotNull(eventObject);
		assertEquals(testData, eventObject.getData());
		assertEquals(testData.getClass().getName(), eventObject.getClazz());
		assertEquals(testData.getClass().getPackage(), eventObject.getPackageName());
	}
}
