package dynamo.backlog.tasks.tvshows;

import dynamo.core.model.Task;
import model.ManagedSeries;

public class DeleteShowTask extends Task {
	
	private ManagedSeries series;

	public DeleteShowTask(ManagedSeries series) {
		super();
		this.series = series;
	}

	public ManagedSeries getSeries() {
		return series;
	}
	
	@Override
	public String toString() {
		return String.format("Delete TV Show %s", series.getName());
	}

}
