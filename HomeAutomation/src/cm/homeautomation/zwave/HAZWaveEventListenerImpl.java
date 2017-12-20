package cm.homeautomation.zwave;

import com.oberasoftware.home.zwave.api.events.ZWaveEvent;
import com.oberasoftware.home.zwave.api.events.devices.DeviceSensorEvent;

public class HAZWaveEventListenerImpl implements HAZWaveEventListener {

	@Override
	public void receiveZWaveEvent(ZWaveEvent event) {
		System.out.println(event);

	}

	@Override
	public void receiveZWaveSensorEvent(DeviceSensorEvent sensorEvent) {
		System.out.println(sensorEvent);

	}

}
