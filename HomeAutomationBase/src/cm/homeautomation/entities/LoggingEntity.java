package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.db.jpa.BasicLogEventEntity;

@Entity(name="LOGGING")
public class LoggingEntity extends BasicLogEventEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1297620939056969127L;
	
	
	private Long id;
	
	public LoggingEntity() {
		super();
	}
	
	public LoggingEntity(final LogEvent wrappedEvent) {
		super(wrappedEvent);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
