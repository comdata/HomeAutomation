package cm.homeautomation.entities;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlID;

@Entity
@Cacheable(false)
public class ScriptingEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@XmlID
	private Long id;

	private String name;
	
	@Column(length=4096)
	private String jsCode;
	
	public enum ScriptingType {
		EVENTHANDLER, UIACTION
	}
	
	private ScriptingType scriptType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJsCode() {
		return jsCode;
	}

	public void setJsCode(String jsCode) {
		this.jsCode = jsCode;
	}

	public ScriptingType getScriptType() {
		return scriptType;
	}

	public void setScriptType(ScriptingType scriptType) {
		this.scriptType = scriptType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
