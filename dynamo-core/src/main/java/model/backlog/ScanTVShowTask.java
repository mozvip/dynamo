package model.backlog;

import dynamo.backlog.tasks.tvshows.ScanFileSystemQueue;
import dynamo.core.DynamoTask;
import dynamo.core.model.Task;
import model.ManagedSeries;

@DynamoTask(queueClass=ScanFileSystemQueue.class)
public class ScanTVShowTask extends Task {

	private ManagedSeries series;

	public ScanTVShowTask(ManagedSeries series) {
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
