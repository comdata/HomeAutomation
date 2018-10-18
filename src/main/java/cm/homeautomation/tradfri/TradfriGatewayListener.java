package cm.homeautomation.tradfri;

/**
 *
 * @author franck
 */
public interface TradfriGatewayListener {

	public void bulb_discovered(LightBulb b);

	public void bulb_discovery_completed();

	public void bulb_discovery_started(int total_devices);

	public void gateway_initializing();

	public void gateway_started();

	public void gateway_stoped();

	public void polling_completed(int bulb_count, int total_time);

	public void polling_started();
}
