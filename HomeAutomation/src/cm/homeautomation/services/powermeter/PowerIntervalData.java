package cm.homeautomation.services.powermeter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;
import javax.xml.bind.annotation.XmlRootElement;

@SqlResultSetMapping(name = "PowerIntervalMapping", classes = @ConstructorResult(targetClass = PowerIntervalData.class, columns = {
		@ColumnResult(name = "KWH", type = BigDecimal.class), @ColumnResult(name = "TIMESLICE", type = Timestamp.class), }))
@XmlRootElement
public class PowerIntervalData {

	private BigDecimal kwh;
	private Timestamp timeslice;

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
