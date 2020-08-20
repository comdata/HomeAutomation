package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.RemoteControlGroupMember;
import cm.homeautomation.entities.RemoteControlGroupMemberType;
import cm.homeautomation.helper.GetterIsSetterCheck;
import cm.homeautomation.helper.TestDataProvider;
import de.a9d3.testing.checks.PublicVariableCheck;
import de.a9d3.testing.executer.SingleThreadExecutor;

class RemoteControlGroupMemberTest {

	@Test
	public void baseTest() {
		SingleThreadExecutor executor = new SingleThreadExecutor();
        Map<Class, String> enumMap=new HashMap<>();
        enumMap.put(RemoteControlGroupMemberType.class, RemoteControlGroupMemberType.SWITCH.toString());
		assertTrue(executor.execute(RemoteControlGroupMember.class, Arrays.asList(
				// new CopyConstructorCheck(),
				// new DefensiveCopyingCheck(),
                // new EmptyCollectionCheck(),
               
				new GetterIsSetterCheck(new TestDataProvider(enumMap, true)),
				// new HashcodeAndEqualsCheck(),
				new PublicVariableCheck(true))));
	}

}
