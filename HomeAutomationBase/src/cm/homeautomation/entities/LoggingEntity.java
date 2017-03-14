package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.logging.log4j.core.appender.db.jpa.BasicLogEventEntity;

@Entity
public class LoggingEntity extends BasicLogEventEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1297620939056969127L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
