package com.github.dynamo.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaScriptManager {
	
	private ScriptEngineManager engineManager = new ScriptEngineManager();
	private ScriptEngine engine = engineManager.getEngineByName("nashorn");	

	static class SingletonHolder {
		static JavaScriptManager instance = new JavaScriptManager();
	}

	public static JavaScriptManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public Object eval( String script ) throws ScriptException {
		return engine.eval( script );
	}
	
	public void put(String key, Object value) {
		engine.put(key, value);
	}

}
