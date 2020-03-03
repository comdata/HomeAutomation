package cm.homeautomation.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GasIntervalData {

	private BigDecimal qm;
	private Timestamp timeslice;

	public GasIntervalData(BigDecimal qm, Timestamp timeslice) {
		this.qm = qm;
		this.timeslice = timeslice;
	}
	
	public BigDecimal getQm() {
		return qm;
	}

	public void setQm(BigDecimal qm) {
		this.qm = qm;
	}

	public Timestamp getTimeslice() {
		return timeslice;
	}

	public void setTimeslice(Timestamp timeslice) {
		this.timeslice = timeslice;
	}

}
