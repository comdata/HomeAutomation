package cm.homeautomation.services.overview;

import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.apache.logging.log4j.LogManager;

public class OverviewEndPointConfiguration extends Configurator {
	private static OverviewWebSocket overviewEndpoint;

	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)getOverviewEndpoint();
    }

	public static OverviewWebSocket getOverviewEndpoint() {
		return overviewEndpoint;
	}

	public static void setOverviewEndpoint(OverviewWebSocket overviewEndpoint) {
		LogManager.getLogger(OverviewEndPointConfiguration.class).info("adding overviewEndpoint");
		OverviewEndPointConfiguration.overviewEndpoint = overviewEndpoint;
	}
	

}
