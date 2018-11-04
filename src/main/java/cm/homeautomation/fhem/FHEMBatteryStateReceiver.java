package cm.homeautomation.fhem;

import java.math.BigDecimal;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.fhem.BatteryStateResult.BatteryState;

public class FHEMBatteryStateReceiver {

	private static final String BATTERY = "battery";

	private FHEMBatteryStateReceiver() {
		// do nothing
	}
	
	public static BatteryStateResult receive(String topic, String messageContent, FHEMDevice fhemDevice) {
		if (topic != null && topic.endsWith(BATTERY)) {

			BigDecimal value = new BigDecimal(messageContent.split(" ")[0]);

			BatteryStateResult batteryStateResult = new BatteryStateResult();
			batteryStateResult.setStateValue(value);
			
			batteryStateResult.setFhemDevice(fhemDevice);
			batteryStateResult.setState(BatteryState.NOTOK);
			if (value.compareTo(BigDecimal.valueOf(25)) > 0) {
				batteryStateResult.setState(BatteryState.OK);
			} else {
				BatteryLowEvent batteryLowEvent=new BatteryLowEvent();
				batteryLowEvent.setBatteryStateResult(batteryStateResult);
				EventBusService.getEventBus().post(batteryLowEvent);
			}

			return batteryStateResult;
		}
		return null;
	}

}
