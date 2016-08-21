package cm.homeautomation.windowblind;

import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.eventbus.EventObject;

public class WindowBlindNotificationServiceTester extends WindowBlindNotificationService {
	
	
	public WindowBlindNotificationServiceTester() {		
			initialize("/Users/mertins/egit/HomeAutomation/pushpad.properties");
			
	}
	
	public static void main(String[] args) {
		WindowBlindNotificationServiceTester windowBlindNotificationServiceTester = new WindowBlindNotificationServiceTester();
		WindowBlindStatus windowBlindStatus = new WindowBlindStatus();
		WindowBlind windowBlind=new WindowBlind();
		windowBlind.setCurrentValue(99);
		windowBlind.setName("Test Window Blind");
		windowBlindStatus.setWindowBlind(windowBlind);
		EventObject eventObject=new EventObject(windowBlindStatus);
		windowBlindNotificationServiceTester.handleWindowBlindChange(eventObject);
	}
}
