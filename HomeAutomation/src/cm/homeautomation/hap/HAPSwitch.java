package cm.homeautomation.hap;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.Service;
import com.beowulfe.hap.accessories.Switch;

import cm.homeautomation.services.actor.ActorService;

public class HAPSwitch implements Switch {

	private Long id;
	private boolean powerState;
	private String label;
	private HomekitCharacteristicChangeCallback subscribeCallback = null;

	public HAPSwitch(String name, boolean initialState, Long id) {
		powerState = initialState;
		label = name;
		this.id = id;

	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return id.intValue() + 2000;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return label;
	}

	@Override
	public void identify() {
		System.out.println("Identifying switch");

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
	public CompletableFuture<Boolean> getSwitchState() {

		return CompletableFuture.completedFuture(powerState);
	}

	@Override
	public CompletableFuture<Void> setSwitchState(boolean powerState) throws Exception {
		this.powerState = powerState;
		if (subscribeCallback != null) {
			subscribeCallback.changed();
		}

		ActorService.getInstance().pressSwitch(id.toString(), (powerState ? "ON" : "OFF"));

		System.out.println("The switch is now " + (powerState ? "on" : "off"));
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void subscribeSwitchState(HomekitCharacteristicChangeCallback callback) {
		this.subscribeCallback = callback;

	}

	@Override
	public void unsubscribeSwitchState() {
		this.subscribeCallback = null;
	}

}
