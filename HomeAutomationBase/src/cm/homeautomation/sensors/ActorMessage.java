package cm.homeautomation.sensors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ActorMessage {
	private String messageType;
	private String houseCode;
	private String switchNo;
	private String status;

	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getHouseCode() {
		return houseCode;
	}
	public void setHouseCode(String houseCode) {
		this.houseCode = houseCode;
	}
	public String getSwitchNo() {
		return switchNo;
	}
	public void setSwitchNo(String switchNo) {
		this.switchNo = switchNo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
