package cm.homeautomation.hap;

import java.util.concurrent.CompletableFuture;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.TemperatureSensor;

public class MockTemperature implements TemperatureSensor {

	private HomekitCharacteristicChangeCallback subscribeCallback = null;

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 42;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return "Temperatur";
	}

	@Override
	public void identify() {
		System.out.println("Identifying temperature");

	}

	@Override
	public String getSerialNumber() {
		// TODO Auto-generated method stub
		return "none";
	}

	@Override
	public String getModel() {
		// TODO Auto-generated method stub
		return "none";
	}

	@Override
	public String getManufacturer() {
		// TODO Auto-generated method stub
		return "none";
	}

	@Override
	public CompletableFuture<Double> getCurrentTemperature() {
		return CompletableFuture.completedFuture(new Double(20));
	}

	@Override
	public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
		this.subscribeCallback = callback;
	}

	@Override
	public void unsubscribeCurrentTemperature() {
		this.subscribeCallback = null;

	}

	@Override
	public double getMinimumTemperature() {
		// TODO Auto-generated method stub
		return new Double(0).doubleValue();
	}

	@Override
	public double getMaximumTemperature() {
		// TODO Auto-generated method stub
		return new Double(35).doubleValue();
	}

}
