package cm.homeautomation.tv.panasonic;

public class Main {

	private static final String TV = "192.168.1.88";

	public static void main(String[] args) throws TVNotReachableException {
		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		boolean alive = panasonicTVBinding.checkAlive(TV);

		if (alive) {
			panasonicTVBinding.sendCommand(TV, "VOLDOWN");
			panasonicTVBinding.sendCommand(TV, "VOLUP");
		}
	}

}
