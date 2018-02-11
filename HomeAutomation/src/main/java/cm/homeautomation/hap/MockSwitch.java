package cm.homeautomation.hap;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.Service;
import com.beowulfe.hap.accessories.Lightbulb;
import com.beowulfe.hap.impl.services.LightbulbService;

public class MockSwitch implements Lightbulb {
	
	private boolean powerState = false;
	private HomekitCharacteristicChangeCallback subscribeCallback = null;

	@Override
	public int getId() {
		return 2;
	}

	@Override
	public String getLabel() {
		return "Test Lightbulb";
	}

	@Override
	public void identify() {
		System.out.println("Identifying light");
	}

	@Override
	public String getSerialNumber() {
		return "none";
	}

	@Override
	public String getModel() {
		return "none";
	}

	@Override
	public String getManufacturer() {
		return "none";
	}

	@Override
	public CompletableFuture<Boolean> getLightbulbPowerState() {
		return CompletableFuture.completedFuture(powerState);
	}

	@Override
	public CompletableFuture<Void> setLightbulbPowerState(boolean powerState)
			throws Exception {
		this.powerState = powerState;
		if (subscribeCallback != null) {
			subscribeCallback.changed();
		}
		
		String url="http://192.168.1.57:8080/HomeAutomation/services/actor/press/2/"+(powerState ? "ON" : "OFF");
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		String HTTP_STATUS_OK = "HTTP/1.1 200";
		
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
			if (!HTTP_STATUS_OK.equals(httpResponse.getStatusLine().toString().subSequence(0, HTTP_STATUS_OK.length()))) {
				System.out.println("HTTP error while send actor press: [" + httpResponse.getStatusLine() + "]");
				
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		System.out.println("The lightbulb is now "+(powerState ? "on" : "off"));
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void subscribeLightbulbPowerState(
			HomekitCharacteristicChangeCallback callback) {
		this.subscribeCallback = callback;
	}

	@Override
	public void unsubscribeLightbulbPowerState() {
		this.subscribeCallback = null;
	}
}
