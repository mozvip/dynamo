package dynamo.core.el;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

public class DynamoELContext extends ELContext {
	
	private Object thisObject = null;
	private Class thisClass = null;

	public DynamoELContext( Object thisObject ) {
		super();
		this.thisObject = thisObject;
		this.thisClass = thisObject.getClass();
	}
	
	public DynamoELContext( Class thisClass ) {
		super();
		this.thisClass = thisClass;
	}

	@Override
	public ELResolver getELResolver() {
		return DynamoELResolver.getInstance();
	}

	@Override
	public FunctionMapper getFunctionMapper() {
		return DynamoFunctionMapper.getInstance();
	}

	@Override
	public VariableMapper getVariableMapper() {
		return new DynamoVariableMapper( thisObject, thisClass );
	}

}
