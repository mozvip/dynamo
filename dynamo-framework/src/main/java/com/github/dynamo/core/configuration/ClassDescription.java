package com.github.dynamo.core.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClassDescription {
	String label() default "";
}
