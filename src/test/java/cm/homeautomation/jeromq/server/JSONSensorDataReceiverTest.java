package cm.homeautomation.jeromq.server;

import javax.persistence.NoResultException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONSensorDataReceiverTest {


	@Test
	public void testReceiveSensorDataNoClassDefined() throws Exception {
		Assertions.assertThrows(NoClassInformationContainedException.class, () -> {
			JSONSensorDataReceiver.receiveSensorData("{}");
		});
	}
	
	@Test
	public void testReceiveSensorSensorDataSaveRequestNoResultException() throws Exception {
		Assertions.assertThrows(NoResultException.class, () -> {
			JSONSensorDataReceiver.receiveSensorData("{\"@c\":\".SensorDataSaveRequest\"}");
		});
	}

	
	// FIXME add more tests
}
