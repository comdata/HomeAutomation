package cm.homeautomation.fhem;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.base.HTTPHelper;
import cm.homeautomation.services.base.HTTPHelper.HTTPHelperCallback;

public class ZWaveNeighborUpdate {

	public class ZWaveGetDevicesCallback implements HTTPHelperCallback {

		@Override
		public void handleResponse(HttpResponse response) {

			try {
				final HttpEntity entity = response.getEntity();
				final String body = EntityUtils.toString(entity);

				final String perDeviceUrl = ConfigurationService.getConfigurationProperty("zwave", "perDeviceUrl");

				final String[] devices = body.split(" ");

				for (int i = 0; i < 5; i++) {
					System.out.println("pass: " + i);
					for (final String device : devices) {
						System.out.println(device);

						if (!"ZWDongle_0".equals(device) && !"nodeList".equals(device) && !"=>".equals(device)
								&& !device.contains("UNKNOWN")) {
							HTTPHelper.performHTTPRequest(perDeviceUrl.replace("%DEVICE%", device.trim()),
									new ZWaveNeighborUpdateCallback(device));

							Thread.sleep(120000);
						}
					}
					Thread.sleep(120000);
				}
				System.out.println("done");
			} catch (ParseException | IOException e) {
				LogManager.getLogger(this.getClass()).error(e);
			} catch (final InterruptedException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}
		}

	}

	public class ZWaveNeighborUpdateCallback implements HTTPHelperCallback {

		private final String device;

		public ZWaveNeighborUpdateCallback(String device) {
			this.device = device;

		}

		@Override
		public void handleResponse(HttpResponse response) {
			try {
				final HttpEntity entity = response.getEntity();
				final String body = EntityUtils.toString(entity);
				System.out.println(device + ": " + body);

			} catch (ParseException | IOException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}

		}

	}

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

		final String devicesUrl = ConfigurationService.getConfigurationProperty("zwave", "devicesUrl");

		HTTPHelper.performHTTPRequest(devicesUrl, new ZWaveGetDevicesCallback());
	}
}
