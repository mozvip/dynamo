package dynamo.backlog.tasks.core;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.ScanFolderTask;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.FolderManager;

public abstract class ScanFolderExecutor<T extends ScanFolderTask> extends TaskExecutor<ScanFolderTask> implements ReportProgress {
	
	protected int totalItems;
	protected int itemsDone;
	
	@Override
	public int getTotalItems() {
		return totalItems;
	}

	@Override
	public int getItemsDone() {
		return itemsDone;
	}

	public ScanFolderExecutor(T task) {
		super( task );
	}
	
	@Override
	public void init() throws Exception {
		if (! Files.isReadable( task.getFolder() )) {
			throw new IOException( String.format("Folder %s is not readable", task.getFolder().toAbsolutePath().toString()));
		}
		String taskLabel = getCurrentLabel();
		setCurrentLabel( String.format("%s - On Hold", taskLabel));
		setCurrentLabel( taskLabel);
	}
	
	@Override
	public void execute() throws Exception {
		Path rootFolder = task.getFolder();

		List<Path> topLevelPaths = getTopLevelPaths( rootFolder );

		if (topLevelPaths != null) {
			totalItems = topLevelPaths.size();
			for (Path path : topLevelPaths) {
				
				if (cancelled) {
					return;
				}
				
				if (Files.isReadable( path )) {
					parsePath( path );
				} else {
					ErrorManager.getInstance().reportWarning( task, String.format("Path %s is unreadable", path.toAbsolutePath().toString()), false );
				}
				itemsDone ++;
			}
		}
	}

	public abstract void parsePath(Path folder) throws Exception;

	@Override
	public void rescheduleTask(ScanFolderTask taskToReschedule) {
		if (isFailed()) {
			BackLogProcessor.getInstance().schedule( taskToReschedule, getNextDate( 30 ), false );
		}
	}

	public abstract Filter<Path> getFileFilter();

	protected List<Path> getTopLevelPaths( Path rootFolder ) throws IOException, InterruptedException {
		List<Path> topLevelPaths = FolderManager.getInstance().getContents( rootFolder, getFileFilter(), false );
		if (topLevelPaths.size() > 0) {
			return topLevelPaths;
		} else {
			// empty
			return null;
		}
	}

}
