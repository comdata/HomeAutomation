package cm.homeautomation.fhem;

import javax.inject.Inject;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.base.HTTPHelper;

public class ZWaveNeighborUpdate {

	@Inject
	ConfigurationService configurationService;

	@Inject
	HTTPHelper httpHelper;

//	public class ZWaveNeighborUpdateCallback implements HTTPHelperCallback {
//
//		private final String device;
//
//		public ZWaveNeighborUpdateCallback(String device) {
//			this.device = device;
//
//		}
//
//		@Override
//		public void handleResponse(HttpResponse response) {
//			try {
//				final HttpEntity entity = response.getEntity();
//				final String body = EntityUtils.toString(entity);
////				//LogManager.getLogger(this.getClass()).debug(device + ": " + body);
//
//			} catch (ParseException | IOException e) {
////				//LogManager.getLogger(this.getClass()).error(e);
//			}
//
//		}
//
//	}

	public static void main(String[] args) {
		new ZWaveNeighborUpdate();
	}

	public static void performNeighborUpdate(String[] args) {
		new ZWaveNeighborUpdate();
	}

	public ZWaveNeighborUpdate() {
		getDevices();
	}

	private void getDevices() {

		final String devicesUrl = configurationService.getConfigurationProperty("zwave", "devicesUrl");

		httpHelper.performHTTPRequest(devicesUrl);
	}
}
