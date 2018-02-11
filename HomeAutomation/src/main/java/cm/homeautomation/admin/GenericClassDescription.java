package cm.homeautomation.admin;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class GenericClassDescription {
	private String name;
	private Map<String, String> fields=new HashMap<String, String>();
	private Map<String, Parameter[]> methods=new HashMap<String,  Parameter[]>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getFields() {
		return fields;
	}
	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}
	public Map<String, Parameter[]> getMethods() {
		return this.methods;
	}
	public void setMethods(Map<String, Parameter[]> methods) {
		this.methods = methods;
	}
	

}
