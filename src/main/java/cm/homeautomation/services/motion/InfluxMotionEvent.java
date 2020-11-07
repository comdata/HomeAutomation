package cm.homeautomation.services.sensors;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import lombok.Getter;
import lombok.Setter;

@Measurement(name = "MotionEvent")
@Getter
@Setter
public class InfluxMotionEvent {
	@Column(tag = true)
	Long roomId;

	@Column
	boolean state;

    @Column
    String externalId;

	@Column(timestamp = true)
    Instant dateTime;
    

}
