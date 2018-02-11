package cm.homeautomation.zwave;

import org.apache.logging.log4j.LogManager;

import com.oberasoftware.home.zwave.api.events.ZWaveEvent;
import com.oberasoftware.home.zwave.api.events.devices.DeviceSensorEvent;

public class HAZWaveEventListenerImpl implements HAZWaveEventListener {

	@Override
	public void receiveZWaveEvent(ZWaveEvent event) {
		LogManager.getLogger(this.getClass()).error(event);

	}

	@Override
	public void receiveZWaveSensorEvent(DeviceSensorEvent sensorEvent) {
		LogManager.getLogger(this.getClass()).error(sensorEvent);

	}

}
