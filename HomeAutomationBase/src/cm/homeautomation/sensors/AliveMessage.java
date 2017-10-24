package cm.homeautomation.sensors;

public class AliveMessage extends JSONSensorDataBase {
	private String client;
	private String ip;
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
}
