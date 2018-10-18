package cm.homeautomation.hap;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

public class Main {
	
	private static final int PORT = 9123;
	
	public static void main(String[] args) {
		try {
			HomekitServer homekit = new HomekitServer(PORT);
			MockAuthInfoService authInfo = new MockAuthInfoService();
			HomekitRoot bridge = homekit.createBridge(authInfo, "Test Bridge5", "TestBridge, Inc.", "G6", "111abe2341");
			bridge.addAccessory(new MockSwitch());
			bridge.allowUnauthenticatedRequests(true);
			bridge.addAccessory(new MockTemperature());
			bridge.start();
			//authInfo.loadUsers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}