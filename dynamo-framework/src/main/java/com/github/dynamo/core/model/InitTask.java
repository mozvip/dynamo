package com.github.dynamo.core.model;

import com.github.dynamo.core.Enableable;

public abstract class InitTask extends Task implements Enableable {
	
	@Override
	public boolean isEnabled() {
		return true;
	}	

}
