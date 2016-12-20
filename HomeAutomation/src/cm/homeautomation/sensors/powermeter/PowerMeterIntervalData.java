package cm.homeautomation.sensors.powermeter;

public class PowerMeterIntervalData {

	private float oneMinute;
	private float sixtyMinute;
	private float fiveMinute;

	public void setOneMinute(float oneMinute) {
		this.oneMinute = oneMinute;
	}
	
	public float getOneMinute() {
		return oneMinute;
	}

	public void setFiveMinute(float fiveMinute) {
		this.fiveMinute = fiveMinute;		
	}
	
	public float getFiveMinute() {
		return fiveMinute;
	}

	public void setSixtyMinute(float sixtyMinute) {
		this.sixtyMinute = sixtyMinute;
	}
	
	public float getSixtyMinute() {
		return sixtyMinute;
	}

}
