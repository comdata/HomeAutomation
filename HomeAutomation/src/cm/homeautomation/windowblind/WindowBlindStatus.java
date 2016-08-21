package cm.homeautomation.windowblind;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.WindowBlind;

@XmlRootElement
public class WindowBlindStatus {
	private WindowBlind windowBlind;

	public WindowBlind getWindowBlind() {
		return windowBlind;
	}

	public void setWindowBlind(WindowBlind windowBlind) {
		this.windowBlind = windowBlind;
	}

}
