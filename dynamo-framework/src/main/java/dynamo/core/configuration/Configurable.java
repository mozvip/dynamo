package dynamo.core.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {
	String ifExpression() default "";
	
	boolean required() default false;

	Class contentsClass() default DEFAULT.class;

	String defaultValue() default "__NULL__";
	
	String defaultLabel() default "None";
	
	boolean folder() default true;

	boolean ordered() default false;
	
	String allowedValues() default "";

	public static final class DEFAULT {}
}
