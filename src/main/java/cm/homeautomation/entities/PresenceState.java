package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

@Entity
public class PresenceState implements HumanMessageGenerationInterface {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private Person person;
	
	private Date date;
	
	private String state;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String getMessageString() {
		
		return person.getName()+" is now: "+state;
	}

	@Override
	public String getTitle() {
		
		return "Presence State";
	}
}
