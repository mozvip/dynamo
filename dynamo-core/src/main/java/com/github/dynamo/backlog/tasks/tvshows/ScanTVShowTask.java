package com.github.dynamo.backlog.tasks.tvshows;

import com.github.dynamo.backlog.tasks.files.ScanFolderTask;
import com.github.dynamo.tvshows.model.ManagedSeries;

public class ScanTVShowTask extends ScanFolderTask {

	private ManagedSeries series;

	public ScanTVShowTask(ManagedSeries series) {
		super( series.getFolder() );
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
		return String.format( "Scan folder of <a href='%s'>%s</a>", series.getRelativeLink(), series.getName() );
	}
	
}
