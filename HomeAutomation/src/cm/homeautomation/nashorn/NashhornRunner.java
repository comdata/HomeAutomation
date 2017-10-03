package cm.homeautomation.nashorn;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class NashhornRunner {
	
	private ScriptEngine engine;
	private static NashhornRunner instance;

	public NashhornRunner() {
		ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName("nashorn");
		
		
	}
	
	public static void main(String[] args) throws Exception {
		// Script Engine Manager
	
		// Evaluate JavaScript code
		getInstance().run("print(\"This is a hello from JavaScript in Java\");");
	}
	
	public void run(String jsCode) throws ScriptException {
		engine.eval(jsCode);
	}

	public static NashhornRunner getInstance() {
		if (instance==null) {
			instance=new NashhornRunner();
		}
		
		return instance;
	}

	public static void setInstance(NashhornRunner instance) {
		NashhornRunner.instance = instance;
	}
}
