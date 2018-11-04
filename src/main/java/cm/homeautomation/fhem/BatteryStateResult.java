package cm.homeautomation.fhem;

import java.math.BigDecimal;

import cm.homeautomation.entities.FHEMDevice;

public class BatteryStateResult {
	
	private BatteryState state;
	private FHEMDevice fhemDevice;
	private BigDecimal stateValue;
	
	public enum BatteryState {
		OK, NOTOK
	}

	public BatteryState getState() {
		return state;
	}

	public void setState(BatteryState state) {
		this.state = state;
	}

	public FHEMDevice getFhemDevice() {
		return fhemDevice;
	}

	public void setFhemDevice(FHEMDevice fhemDevice) {
		this.fhemDevice = fhemDevice;
	}
	
	public void setStateValue(BigDecimal stateValue) {
		this.stateValue = stateValue;
	}

	public BigDecimal getStateValue() {
		return stateValue;
	}
}
