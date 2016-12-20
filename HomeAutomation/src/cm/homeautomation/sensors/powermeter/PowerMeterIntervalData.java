package cm.homeautomation.sensors.powermeter;

public class PowerMeterIntervalData {

	private float oneMinute;
	private float sixtyMinute;
	private float fiveMinute;
	private float yesterday;
	private float lastSevenDays;

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

	public void setYesterday(float yesterday) {
		this.yesterday = yesterday;
	}
	
	public float getYesterday() {
		return yesterday;
	}

	public void setLastSevenDays(float lastSevenDays) {
		this.lastSevenDays = lastSevenDays;
	}

	public float getLastSevenDays() {
		return lastSevenDays;
	}
	
}
