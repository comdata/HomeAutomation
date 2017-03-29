package cm.homeautomation.tv;

import cm.homeautomation.sensors.base.TechnicalSensor;
import cm.homeautomation.tv.panasonic.PanasonicTVBinding;

public class PanasonicTVSensor implements TechnicalSensor {

	private String tvIp;
	private PanasonicTVBinding panasonicTVBinding;
	private String type;

	public PanasonicTVSensor(String type,String tvIp) {
		this.type = type;
		this.setTvIp(tvIp);
		panasonicTVBinding = new PanasonicTVBinding();
	}
	
	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return (getPanasonicTVBinding().checkAlive(getTvIp())) ? "1": "0";
	}

	@Override
	public String getType() {
		return type;
	}

	public String getTvIp() {
		return tvIp;
	}

	public void setTvIp(String tvIp) {
		this.tvIp = tvIp;
	}

	public PanasonicTVBinding getPanasonicTVBinding() {
		return panasonicTVBinding;
	}

	public void setPanasonicTVBinding(PanasonicTVBinding panasonicTVBinding) {
		this.panasonicTVBinding = panasonicTVBinding;
	}

}
