package cm.homeautomation.services.sensors;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import lombok.Getter;
import lombok.Setter;

@Measurement(name = "SensorData")
@Getter
@Setter
public class InfluxSensorData {
	@Column(tag = true)
	Long roomId;

	@Column(tag = true)
	String room;

	@Column(tag = true)
	String sensorName;

	@Column(tag = true)
	Long sensorId;

	@Column
	String value;

	@Column(timestamp = true)
	Instant dateTime;
}
