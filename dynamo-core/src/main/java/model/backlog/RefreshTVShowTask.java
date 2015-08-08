package model.backlog;

import dynamo.backlog.queues.TVDBQueue;
import dynamo.core.model.Task;
import model.ManagedSeries;

public class RefreshTVShowTask extends Task {
	
	private ManagedSeries series;

	public RefreshTVShowTask( ManagedSeries series ) {
		this.series = series;
	}

	public ManagedSeries getSeries() {
		return series;
	}
	
	public void setSeries(ManagedSeries series) {
		this.series = series;
	}

	@Override
	public String toString() {
		return String.format( "Refresh <a href='%s'>%s</a> from TVDB", series.getRelativeLink(), series.getName() );
	}
	
	
	@Override
	public Class getQueueClass() {
		return TVDBQueue.class;
	}
}
