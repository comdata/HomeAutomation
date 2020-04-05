package cm.homeautomation.services.windowblind;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.WindowBlind;

@XmlRootElement
public class WindowBlindsList {
	private List<WindowBlind> windowBlinds;

	public List<WindowBlind> getWindowBlinds() {
		if (windowBlinds==null) {
			windowBlinds=new ArrayList<>();
		}
		return windowBlinds;
	}

	public void setWindowBlinds(List<WindowBlind> windowBlinds) {
		this.windowBlinds = windowBlinds;
	}
}
