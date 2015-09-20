package model.backlog;

import dynamo.backlog.queues.TVDBQueue;
import dynamo.core.DynamoTask;
import dynamo.core.model.Task;
import dynamo.model.tvshows.TVShowManager;
import model.ManagedSeries;

@DynamoTask(queueClass=TVDBQueue.class)
public class RefreshTVShowTask extends Task {
	
	private ManagedSeries series = null;

	public RefreshTVShowTask( String tvShowId ) {
		this.series = TVShowManager.getInstance().findTVShow( tvShowId );
	}
	
	public ManagedSeries getSeries() {
		return series;
	}

	@Override
	public String toString() {
		return String.format( "Refresh <a href='%s'>%s</a> from TVDB", series.getRelativeLink(), series.getName() );
	}

}
