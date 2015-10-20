package dynamo.backlog.tasks.tvshows;

import dynamo.core.model.Task;
import model.ManagedSeries;

public class DeleteShowTask extends Task {
	
	private ManagedSeries series;
	private boolean deleteFiles = false;

	public DeleteShowTask(ManagedSeries series, boolean deleteFiles) {
		super();
		this.series = series;
		this.deleteFiles = deleteFiles;
	}

	public ManagedSeries getSeries() {
		return series;
	}
	
	public boolean isDeleteFiles() {
		return deleteFiles;
	}
	
	@Override
	public String toString() {
		if (deleteFiles) {
			return String.format("Delete TV Show %s, deleting files", series.getName());
		} else {
			return String.format("Delete TV Show %s", series.getName());
		}
	}

}
