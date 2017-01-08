package com.github.dynamo.core.services;

import com.github.dynamo.core.configuration.ClassDescription;

public class LabelledClass {

	private String label;
	private Class<?> klass;

	public LabelledClass( Class<?> klass ) {
		ClassDescription annotation = klass.getAnnotation( ClassDescription.class );
		this.label = annotation != null ? annotation.label() : klass.getName();		
		this.klass = klass;
	}

	public String getLabel() {
		return label;
	}

	public Class<?> getKlass() {
		return klass;
	}

}
