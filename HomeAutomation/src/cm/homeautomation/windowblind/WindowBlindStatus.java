package cm.homeautomation.windowblind;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.HumanMessageGenerationInterface;
import cm.homeautomation.entities.WindowBlind;

@XmlRootElement
public class WindowBlindStatus implements HumanMessageGenerationInterface {
	private WindowBlind windowBlind;

	public WindowBlind getWindowBlind() {
		return windowBlind;
	}

	public void setWindowBlind(WindowBlind windowBlind) {
		this.windowBlind = windowBlind;
	}

	@Override
	public String getMessageString() {
		if (this.windowBlind != null) {
			return "Window blind: "+this.windowBlind.getName()+" new status: "+ this.windowBlind.getCurrentValue();
		}
		return null;
	}

}
