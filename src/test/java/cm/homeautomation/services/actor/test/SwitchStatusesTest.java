package cm.homeautomation.services.actor.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cm.homeautomation.entities.Switch;
import cm.homeautomation.services.actor.SwitchStatuses;

public class SwitchStatusesTest {

	@Test
	public void testCreate() throws Exception {
		SwitchStatuses switchStatuses = new SwitchStatuses();
		
		assertNotNull(switchStatuses);
		assertNotNull(switchStatuses.getSwitchStatuses());
		assertTrue(switchStatuses.getSwitchStatuses().isEmpty());
	}
	
	@Test
	public void testCreateWithEntries() throws Exception {
		SwitchStatuses switchStatuses = new SwitchStatuses();
		
		List<Switch> switchStatusList=new ArrayList<Switch>();
		Switch aSwitch=new Switch();
		aSwitch.setName("Test Switch");
		switchStatusList.add(aSwitch);
		switchStatuses.setSwitchStatuses(switchStatusList);
		assertNotNull(switchStatuses);
		assertNotNull(switchStatuses.getSwitchStatuses());
		assertTrue(switchStatuses.getSwitchStatuses().size()==1);
		assertEquals(aSwitch, (Switch)switchStatuses.getSwitchStatuses().get(0));
	}
}
