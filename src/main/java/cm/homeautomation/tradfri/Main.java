package cm.homeautomation.tradfri;

public class Main {

	protected static String gateway_ip = "192.168.1.115";
	protected static String security_key = "BOnI8cZukkydrxBp";

	public static void main(final String[] args) {

		final TradfriGateway gw = new TradfriGateway(gateway_ip, security_key);

		final TradfriGatewayListener gatewayListener = new TradfriGatewaylistenerImpl();
		gw.addTradfriGatewayListener(gatewayListener);
		gw.initCoap();
		gw.dicoverBulbs();

		/*
		 * for (final LightBulb b : gw.bulbs) {
		 * 
		 * // System.out.println(b.getId()); System.out.print(b.toString());
		 * System.out.println("\t on: " + b.isOn()); }
		 */

		System.exit(0);
	}

}