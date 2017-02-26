package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GasMeterPing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	private Date timestamp;
	
	private int gasMeter;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getGasMeter() {
		return gasMeter;
	}

	public void setGasMeter(int gasMeter) {
		this.gasMeter = gasMeter;
	}
	
}
