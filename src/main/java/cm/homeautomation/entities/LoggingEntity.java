package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.db.jpa.BasicLogEventEntity;
import org.eclipse.persistence.annotations.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="LOGGING")
@Index(name = "ix_timemillis", columnNames = { "timeMillis" })
@Index(name = "ix_source", columnNames = { "source", "timeMillis" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoggingEntity extends BasicLogEventEntity {

	/**
	 * 
	 */
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = 0L;
	
	public LoggingEntity(final LogEvent wrappedEvent) {
		super(wrappedEvent);
	}

	
}
