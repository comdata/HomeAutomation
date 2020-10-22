package cm.homeautomation.eventbus;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class EventBusEndpointConfigurator extends Configurator {
	private static EventBusEndpoint eventBusEndpoint;

	@SuppressWarnings("unchecked")
	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)getEventBusEndpoint();
    }

	public static EventBusEndpoint getEventBusEndpoint() {
		return eventBusEndpoint;
	}

	public static void setEventBusEndpoint(EventBusEndpoint eventBusEndpoint) {
//		LogManager.getLogger(EventBusEndpointConfigurator.class).info("adding eventBusEndpoint");
		EventBusEndpointConfigurator.eventBusEndpoint = eventBusEndpoint;
	}
}
