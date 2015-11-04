package cm.homeautomation.sensors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RFEvent {
	private int code;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
