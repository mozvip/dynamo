package com.github.dynamo.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class Task {

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.toString().equals( toString() );
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
