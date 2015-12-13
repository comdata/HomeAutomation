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

import cm.homeautomation.services.actor.ActorService;

public class HAPSwitch implements Lightbulb {
	
	private boolean powerState = false;
	private HomekitCharacteristicChangeCallback subscribeCallback = null;
	private String label;
	private Long id;

	public HAPSwitch() {
	}
	
	public HAPSwitch(String name,boolean initialState, Long id) {
		powerState=initialState;
		label=name;
		this.id=id;
	}
	
	@Override
	public int getId() {
		return id.intValue()+2000;
	}

	@Override
	public String getLabel() {
		return label;
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
		
		ActorService.getInstance().pressSwitch(id.toString(), (powerState ? "ON" : "OFF"));
		
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
