
package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityReference;


@Entity
@Table(indexes = { @Index(name = "sensorId", columnList = "SENSOR_ID, VALIDTHRU") })
public class SensorData {
	@Id
	@GeneratedValue(generator = "SensorData")
	@TableGenerator(name = "SensorData", table = "SEQUENCE", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SENSOR_DATA", allocationSize = 10)
	private Long id;

	private String value;

	@ManyToOne
	@JoinColumn(name = "SENSOR_ID", nullable = false)
	
	@JsonIdentityReference
	@JsonBackReference
	private Sensor sensor;

	private Date dateTime = new Date();

	private Date validThru = new Date();

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	@JsonIdentityReference
	@JsonBackReference
	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public Date getValidThru() {
		return validThru;
	}

	public void setValidThru(Date validThru) {
		this.validThru = validThru;
	}
}
