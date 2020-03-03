package cm.homeautomation.sensors;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude (JsonInclude.Include.USE_DEFAULTS)
@XmlRootElement
public class ActorMessage {
	private String messageType;
	private String houseCode;
	private String switchNo;
	private String status;
	private Long id;

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
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

}
