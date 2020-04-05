package cm.homeautomation.services.windowblind;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

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
			return "Window blind: "+this.windowBlind.getName()+" new status: "+ this.windowBlind.getCurrentValue()+"%";
		}
		return null;
	}

	@Override
	public String getTitle() {
		
		return "Window Blind ("+windowBlind.getName()+") changed position";
	}

}
