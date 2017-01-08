package com.github.dynamo.backlog.tasks.tvshows;

import com.github.dynamo.core.LogQueuing;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.tvshows.model.ManagedSeries;

public class DeleteShowTask extends Task implements LogQueuing {
	
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
