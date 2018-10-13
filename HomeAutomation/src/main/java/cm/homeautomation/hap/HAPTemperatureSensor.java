package cm.homeautomation.hap;

import java.util.concurrent.CompletableFuture;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.TemperatureSensor;
import com.beowulfe.hap.impl.services.TemperatureSensorService;

public class HAPTemperatureSensor implements TemperatureSensor {
	private HomekitCharacteristicChangeCallback subscribeCallback = null;
	private String label;
	private Long id;
	private Double temperature=new Double(0);

	public HAPTemperatureSensor(String name, Long id) {

		label = name;
		this.id = id;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return id.intValue()+100;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return label;
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
		return CompletableFuture.completedFuture(getTemperature());
	}

	@Override
	public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
		this.setSubscribeCallback(callback);
	}

	@Override
	public void unsubscribeCurrentTemperature() {
		this.setSubscribeCallback(null);

	}

	@Override
	public double getMinimumTemperature() {
		return Double.valueOf(0);
	}

	@Override
	public double getMaximumTemperature() {
		return Double.valueOf(50);
	}

	/**
	 * @return the subscribeCallback
	 */
	public HomekitCharacteristicChangeCallback getSubscribeCallback() {
		return subscribeCallback;
	}

	/**
	 * @param subscribeCallback the subscribeCallback to set
	 */
	public void setSubscribeCallback(HomekitCharacteristicChangeCallback subscribeCallback) {
		this.subscribeCallback = subscribeCallback;
	}

	/**
	 * @return the temperature
	 */
	public Double getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
}
