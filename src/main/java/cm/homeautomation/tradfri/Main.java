package cm.homeautomation.tradfri;

public class Main {

	protected static String gateway_ip = "192.168.1.115";
	protected static String security_key = "";

	public static void main(final String[] args) {

		final TradfriGateway gw = new TradfriGateway(gateway_ip, security_key);

		final TradfriGatewayListener gatewayListener = new TradfriGatewaylistenerImpl();
		gw.addTradfriGatewayListener(gatewayListener);
		gw.initCoap();
		gw.dicoverBulbs();



		System.exit(0);
	}

}