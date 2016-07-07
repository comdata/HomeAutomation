package cm.homeautomation.eventbus;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class EventBusEndpointConfigurator extends Configurator {
	private static EventBusEndpoint eventBusEndpoint=new EventBusEndpoint();

	@SuppressWarnings("unchecked")
	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)eventBusEndpoint;
    }
}
