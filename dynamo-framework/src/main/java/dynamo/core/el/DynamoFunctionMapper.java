package dynamo.core.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.el.FunctionMapper;

import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

public class DynamoFunctionMapper extends FunctionMapper {

	private Map<String, Method> functionMap = new HashMap<String, Method>();

	static class SingletonHolder {
		static DynamoFunctionMapper instance = new DynamoFunctionMapper();
	}

	public static DynamoFunctionMapper getInstance() {
		return SingletonHolder.instance;
	}	
	
	private DynamoFunctionMapper() {
		try {
			functionMap.put("dynamo:activePlugin", DynamoFunctionMapper.class.getMethod("getActivePlugin", String.class));
			functionMap.put("dynamo:isActive", DynamoFunctionMapper.class.getMethod("isActive", String.class));
			functionMap.put("dynamo:isActiveClass", DynamoFunctionMapper.class.getMethod("isActive", Class.class));
		} catch (NoSuchMethodException | SecurityException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}
	
	public static boolean isActive( String executorClassName ) throws ClassNotFoundException {
		return isActive( (Class<? extends TaskExecutor<?>>) Class.forName( executorClassName ));
	}
	
	public static boolean isActive( Class<? extends TaskExecutor<?>> executorClass ) {
		return ConfigurationManager.getInstance().isActive( executorClass );
	}

	public static Class<? extends TaskExecutor> getActivePlugin( String taskName ) throws ClassNotFoundException {
		return ConfigurationManager.getInstance().getActivePlugin( (Class<? extends Task>) Class.forName(taskName) );
	}

	@Override
	public Method resolveFunction(String prefix, String localName) {
		String key = prefix + ":" + localName;
		return functionMap.get(key);
	}

}
