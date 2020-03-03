package cm.homeautomation.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PowerIntervalData {

	private BigDecimal kwh;
	private Timestamp timeslice;

	public PowerIntervalData(BigDecimal kwh, Timestamp timeslice) {
		this.kwh = kwh;
		this.timeslice = timeslice;
	}
	
	public BigDecimal getKwh() {
		return kwh;
	}

	public void setKwh(BigDecimal kwh) {
		this.kwh = kwh;
	}

	public Timestamp getTimeslice() {
		return timeslice;
	}

	public void setTimeslice(Timestamp timeslice) {
		this.timeslice = timeslice;
	}

}
