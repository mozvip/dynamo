package dynamo.core.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {
	String category();
	String name() default "";

	Class contentsClass() default DEFAULT.class;

	String defaultValue() default "__NULL__";
	String required() default "false";
	String disabled() default "false";
	
	String defaultLabel() default "None";
	
	boolean folder() default true;

	boolean ordered() default false;
	
	String allowedValues() default "";

	public static final class DEFAULT {}
}
