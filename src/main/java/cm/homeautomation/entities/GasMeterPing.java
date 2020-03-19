package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Index;

@Entity
public class GasMeterPing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Index(name = "ix_timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	private int gasMeter;

	public int getGasMeter() {
		return gasMeter;
	}

	public Long getId() {
		return id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setGasMeter(int gasMeter) {
		this.gasMeter = gasMeter;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
