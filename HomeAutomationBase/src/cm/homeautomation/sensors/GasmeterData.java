package cm.homeautomation.sensors;
/**
 * gas meter data object
 * 
 * @author christoph
 *
 */
public class GasmeterData extends JSONSensorDataBase {
	private int gasMeter;

	public int getPGasMeter() {
		return gasMeter;
	}

	public void setGasMeter(int powermeter) {
		this.gasMeter = powermeter;
	}
	
	
}
