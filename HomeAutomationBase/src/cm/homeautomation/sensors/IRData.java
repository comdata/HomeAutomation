package cm.homeautomation.sensors;

import java.util.List;

public class IRData extends JSONSensorDataBase {
	private String type;
	private String typeClear;
	private String address;
	private String command;
	private String data;
	
	private List<Integer> rawCode;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeClear() {
		return typeClear;
	}

	public void setTypeClear(String typeClear) {
		this.typeClear = typeClear;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<Integer> getRawCode() {
		return rawCode;
	}

	public void setRawCode(List<Integer> values) {
		this.rawCode = values;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
