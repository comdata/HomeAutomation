package cm.homeautomation.services.tv;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TVCommandEvent {
	
	private String tvIp;
	private String command;

	public TVCommandEvent() {
	}

	public TVCommandEvent(String tvIp, String command) {
		this.setTvIp(tvIp);
		this.setCommand(command);
	}

	public String getTvIp() {
		return tvIp;
	}

	public void setTvIp(String tvIp) {
		this.tvIp = tvIp;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
