package cm.homeautomation.services.powermeter;

import java.util.Date;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

@SqlResultSetMapping(name = "PowerIntervalMapping", classes = @ConstructorResult(targetClass = PowerIntervalData.class, columns = {
		@ColumnResult(name = "KWH", type = Long.class), @ColumnResult(name = "TIMESLICE", type = Date.class), }))

public class PowerIntervalData {

	private Long kwh;
	private Date timeslice;

	public Long getKwh() {
		return kwh;
	}

	public void setKwh(Long kwh) {
		this.kwh = kwh;
	}

	public Date getTimeslice() {
		return timeslice;
	}

	public void setTimeslice(Date timeslice) {
		this.timeslice = timeslice;
	}

}
