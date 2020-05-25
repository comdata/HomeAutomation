package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;



@Entity

public class IRCommand {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	private Long id;

	private String type;
	private String typeClear;
	private String address;
	private String command;
	private int repeats;
	private long repeatDelay;
	private String data;
	
	@JoinColumn(nullable = true)
	@OneToOne(optional=true)
	private Switch irSwitch;
	
	@OneToOne(optional=true)
	@JoinColumn(nullable=true)
	
	private IRCommand followUpCommand;

	
	@ElementCollection
	@CollectionTable(name = "IRCOMMANDVALUES", joinColumns=@JoinColumn(name="IRCOMMAND_ID"))
	@Column(name="codeValues")
	private List<String> values;
	private String name;
	private String description;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeClear() {
		return typeClear;
	}

	public void setTypeClear(String typeClear) {
		this.typeClear = typeClear;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getRepeats() {
		return repeats;
	}

	public void setRepeats(int repeats) {
		this.repeats = repeats;
	}

	public long getRepeatDelay() {
		return repeatDelay;
	}

	public void setRepeatDelay(long repeatDelay) {
		this.repeatDelay = repeatDelay;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public IRCommand getFollowUpCommand() {
		return followUpCommand;
	}

	public void setFollowUpCommand(IRCommand followUpCommand) {
		this.followUpCommand = followUpCommand;
	}

}
