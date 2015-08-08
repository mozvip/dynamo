package dynamo.core.model;

import dynamo.core.Enableable;

public abstract class InitTask extends Task implements Enableable {
	
	@Override
	public boolean isEnabled() {
		return true;
	}	

}
