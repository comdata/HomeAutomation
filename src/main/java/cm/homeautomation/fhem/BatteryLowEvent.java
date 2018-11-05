package cm.homeautomation.fhem;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class BatteryLowEvent implements HumanMessageGenerationInterface {

	private BatteryStateResult batteryStateResult;

	@Override
	public String getTitle() {
		return "Battery Low";
	}

	@Override
	public String getMessageString() {
		return "Battery Low for "+batteryStateResult.getFhemDevice().getName()+" value: "+batteryStateResult.getStateValue();
	}

	public void setBatteryStateResult(BatteryStateResult batteryStateResult) {
		this.batteryStateResult = batteryStateResult;
	}
	
	public BatteryStateResult getBatteryStateResult() {
		return batteryStateResult;
	}

}
