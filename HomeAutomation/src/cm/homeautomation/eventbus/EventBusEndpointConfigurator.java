package cm.homeautomation.eventbus;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class EventBusEndpointConfigurator extends Configurator {
	private static EventBusEndpoint actorEndpoint=new EventBusEndpoint();

	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)actorEndpoint;
    }
}
