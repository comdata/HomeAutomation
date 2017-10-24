package cm.homeautomation.sensors;

import java.util.List;

public class IRData extends JSONSensorDataBase {
	private String type;
	private String typeClear;
	private int address;
	private int command;
	private List<Integer> values;

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

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	public List<Integer> getValues() {
		return values;
	}

	public void setValues(List<Integer> values) {
		this.values = values;
	}
}
