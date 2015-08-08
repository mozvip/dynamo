package dynamo.core.tasks;

import java.lang.reflect.Method;

import dynamo.core.model.Task;

public class InvokeMethodTask extends Task {
	
	private Object object;
	private Method method;
	private Object[] params;
	private String description;
	
	public InvokeMethodTask(Object object, String methodName, String description) throws NoSuchMethodException, SecurityException {
		this(object, methodName, description, null);
	}

	public InvokeMethodTask(Object object, String methodName, String description, Object... params) throws NoSuchMethodException, SecurityException {
		super();
		this.object = object;
		if (params != null) {
			Class[] parameterTypes = new Class[ params.length ];
			int i=0;
			for (Class paramType : parameterTypes) {
				parameterTypes[i] = params[i].getClass();
				i++;
			}
			for (Method method : object.getClass().getMethods()) {
				if (method.getName().equals( methodName) && method.getParameterTypes().length == parameterTypes.length) {
					boolean compatibleMethod = true;
					for (i=0; i<parameterTypes.length; i++) {
						if (!method.getParameterTypes()[i].isAssignableFrom( parameterTypes[i])) {
							compatibleMethod = false;
							break;
						}
					}
					
					if (compatibleMethod) {
						this.method = method;
						break;
					}
				}
			}
		} else {
			this.method = object.getClass().getMethod( methodName );
		}
		if (this.method == null) {
			throw new NoSuchMethodException();
		}
		this.params = params;
		this.description = description;
	}

	public Object getObject() {
		return object;
	}
	public Method getMethod() {
		return method;
	}
	public Object[] getParams() {
		return params;
	}
	
	@Override
	public String toString() {
		if ( description != null ) {
			return description;
		} else {
			return String.format("Invoking method %s on object %s", method.toGenericString(), object.toString() );
		}
	}
	

}
