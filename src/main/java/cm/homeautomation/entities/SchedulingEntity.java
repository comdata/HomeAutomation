package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SchedulingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String pattern;
	private String taskAction;

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getTaskAction() {
		return taskAction;
	}

	public void setTaskAction(String taskAction) {
		this.taskAction = taskAction;
	}

}
