package com.github.dynamo.backlog.tasks.core;

import java.nio.file.Files;
import java.nio.file.Path;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.tvshows.DeleteShowTask;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.github.dynamo.tvshows.model.UnrecognizedFolder;

public class RemoveOrphansExecutor extends TaskExecutor<RemoveOrphansTask> {

	public RemoveOrphansExecutor(RemoveOrphansTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		if (TVShowManager.getInstance().isEnabled()) {
			
			for (UnrecognizedFolder unrecognizedFolder : TVShowManager.getInstance().getUnrecognizedFolders()) {
				boolean found = false;
				if (Files.exists(unrecognizedFolder.getPath())) {
					for (Path folder : TVShowManager.getInstance().getFolders()) {
						if (unrecognizedFolder.getPath().startsWith( folder )) {
							found = true;
							break;
						}
					}
				}
				
				if (!found) {
					TVShowManager.getInstance().deleteUnrecognizedFolder( unrecognizedFolder.getPath() );
				}
			}
			
			for (ManagedSeries tvShow : TVShowManager.getInstance().getManagedSeries()) {
				boolean found = false;
				for (Path folder : TVShowManager.getInstance().getFolders()) {
					if (tvShow.getFolder().startsWith( folder )) {
						found = true;
						break;
					}
				}
				
				if (!found) {
					BackLogProcessor.getInstance().schedule( new DeleteShowTask( tvShow, false ), false );
				}
			}
		}
		
	}

}
