package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class HAZWaveNode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nodeId;
	private String name;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

}
