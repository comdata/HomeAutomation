package cm.homeautomation.sensors;

import java.util.Date;

public class SensorValue {
	private Date dateTime;
	private String value;
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
