package dynamo.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.DynamoDefaultQueue;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DynamoTask {
	
	public Class<? extends AbstractDynamoQueue> queueClass() default DynamoDefaultQueue.class;

}
