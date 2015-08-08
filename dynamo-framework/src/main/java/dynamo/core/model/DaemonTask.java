package dynamo.core.model;

import dynamo.core.Enableable;

public abstract class DaemonTask extends Task implements Enableable {
	
	private int minutesFrequency;

	public DaemonTask( int minutesFrequency ) {
		this.minutesFrequency = minutesFrequency;
	}
	
	public int getMinutesFrequency() {
		return minutesFrequency;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
