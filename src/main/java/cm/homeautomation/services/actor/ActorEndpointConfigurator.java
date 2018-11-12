package cm.homeautomation.services.actor;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class ActorEndpointConfigurator extends Configurator {
	private static ActorEndpoint actorEndpoint=new ActorEndpoint();

	@SuppressWarnings("unchecked")
	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)actorEndpoint;
    }
}
