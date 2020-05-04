package cm.homeautomation.sensors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PowerMeterData extends JSONSensorDataBase {
	private int powermeter;

	public int getPowermeter() {
		return powermeter;
	}

	public void setPowermeter(int powermeter) {
		this.powermeter = powermeter;
	}
	
	
}
