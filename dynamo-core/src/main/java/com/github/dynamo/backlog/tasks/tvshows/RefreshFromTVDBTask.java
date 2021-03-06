package com.github.dynamo.backlog.tasks.tvshows;

import com.github.dynamo.core.model.Task;
import com.github.dynamo.tvshows.model.ManagedSeries;

public class RefreshFromTVDBTask extends Task {
	
	private ManagedSeries series = null;

	public RefreshFromTVDBTask( ManagedSeries series ) {
		this.series = series;
	}

	public ManagedSeries getSeries() {
		return series;
	}

	@Override
	public String toString() {
		return String.format( "Refresh <a href='%s'>%s</a> from TVDB", series.getRelativeLink(), series.getName() );
	}

}
