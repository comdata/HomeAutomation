package cm.homeautomation.services.actor.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import cm.homeautomation.services.actor.ActorEndpointConfigurator;

public class ActorEndpointConfiguratorTest {

	@Test
	public void testGetEndpointInstance() throws Exception {
		ActorEndpointConfigurator actorEndpointConfigurator = new ActorEndpointConfigurator();
		
		assertNotNull(actorEndpointConfigurator);
		
		Object endpointInstance = actorEndpointConfigurator.getEndpointInstance(null);
		assertNotNull(endpointInstance);
	}

}
