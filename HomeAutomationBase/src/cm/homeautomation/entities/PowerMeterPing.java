package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

@Entity
@Indexes({ @Index(name = "ix_timestamp", columnNames = { "timestamp", "powerCounter" }),
		@Index(name = "ix_timestamp_compress", columnNames = { "timestamp", "powerCounter", "compressed" }) })
public class PowerMeterPing {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
	private int powermeter;

	@Column(columnDefinition = "int default 1")
	private int powerCounter = 1;

	@Column(columnDefinition = "TINYINT default 0")
	private boolean compressed = false;

	public Long getId() {
		return id;
	}

	public int getPowerCounter() {
		return powerCounter;
	}

	public int getPowermeter() {
		return powermeter;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPowerCounter(int powerCounter) {
		this.powerCounter = powerCounter;
	}

	public void setPowermeter(int powermeter) {
		this.powermeter = powermeter;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
