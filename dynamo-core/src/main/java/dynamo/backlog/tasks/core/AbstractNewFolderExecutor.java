package dynamo.backlog.tasks.core;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dynamo.core.manager.ErrorManager;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.model.backlog.core.NewFolderTask;

public abstract class AbstractNewFolderExecutor<T extends NewFolderTask> extends TaskExecutor<NewFolderTask> implements ReportProgress {
	
	NewFolderTask newFolderTask;
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

	public AbstractNewFolderExecutor(NewFolderTask task) {
		super( task );
		newFolderTask = task;
	}
	
	@Override
	public void execute() throws Exception {
		Path rootFolder = newFolderTask.getFolder();
		if (!Files.exists( rootFolder )) {
			ErrorManager.getInstance().reportWarning( String.format("Folder %s does not exist or is unreachable", rootFolder.toAbsolutePath().toString()) );
			return;
		}

		List<Path> topLevelPaths = getTopLevelPathList( rootFolder );

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
	public void rescheduleTask(NewFolderTask taskToReschedule) {
		if (isFailed()) {
			taskToReschedule.setMinDate( getNextDate( 30 ) );
			queue( taskToReschedule, false );
		}
	}

	public abstract Filter<Path> getFileFilter();

	protected List<Path> getTopLevelPathList( Path rootFolder ) throws IOException {
		List<Path> thisLevelFolders = new ArrayList<Path>();
		try (DirectoryStream<Path> ds = getFileFilter() != null ? Files.newDirectoryStream( rootFolder, getFileFilter() ) : Files.newDirectoryStream( rootFolder )) {
			for (Path currentPath : ds) {
				thisLevelFolders.add( currentPath );
			}
		}
		if (thisLevelFolders.size() > 1) {
			return thisLevelFolders;
		} else if (thisLevelFolders.size() == 1) {
			return getTopLevelPathList( thisLevelFolders.get( 0 ) );
		} else {
			// empty
			return null;
		}
	}

}
