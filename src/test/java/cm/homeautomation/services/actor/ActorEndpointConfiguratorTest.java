package cm.homeautomation.services.actor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ActorEndpointConfiguratorTest {

	@Test
	public void testGetEndpointInstance() throws Exception {
		ActorEndpointConfigurator actorEndpointConfigurator = new ActorEndpointConfigurator();
		
		assertNotNull(actorEndpointConfigurator);
	}

}
