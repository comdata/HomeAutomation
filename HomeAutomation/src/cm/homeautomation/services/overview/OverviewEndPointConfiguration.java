package cm.homeautomation.services.overview;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class OverviewEndPointConfiguration extends Configurator {
	private static OverviewWebSocket overviewEndpoint=new OverviewWebSocket();

	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)overviewEndpoint;
    }
}
