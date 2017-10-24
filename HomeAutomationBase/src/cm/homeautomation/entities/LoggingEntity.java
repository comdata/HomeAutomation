package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.db.jpa.BasicLogEventEntity;
import org.eclipse.persistence.annotations.Index;

@Entity
@Table(name="LOGGING")
@Index(name = "ix_timemillis", columnNames = { "timeMillis" })
public class LoggingEntity extends BasicLogEventEntity {

	/**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private long id = 0L;
    
	public LoggingEntity() {
		super();
	}
	
	public LoggingEntity(final LogEvent wrappedEvent) {
		super(wrappedEvent);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
