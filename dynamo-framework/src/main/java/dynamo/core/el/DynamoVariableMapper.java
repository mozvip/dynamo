package dynamo.core.el;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

public class DynamoVariableMapper extends VariableMapper {

	Map<String, ValueExpression> map = Collections.emptyMap();

	public DynamoVariableMapper() {
	}

	public DynamoVariableMapper( Object thisObject, Class thisClass ) {
		if (thisObject != null ) {
			ValueExpression expression = ExpressionFactory.newInstance().createValueExpression( thisObject, thisObject.getClass() );
			setVariable("this", expression);
		}
		if (thisClass != null) {
			ValueExpression expression = ExpressionFactory.newInstance().createValueExpression( thisClass, Class.class );
			setVariable("thisClass", expression);
		}
	}

	@Override
	public ValueExpression resolveVariable(String variable) {
		return this.map.get(variable);
	}

	@Override
	public ValueExpression setVariable(String variable,
			ValueExpression expression) {
		if (this.map.isEmpty()) {
			this.map = new HashMap<String, ValueExpression>();
		}
		return this.map.put(variable, expression);
	}

}
