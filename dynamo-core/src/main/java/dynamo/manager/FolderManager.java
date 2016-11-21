package dynamo.manager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.TaskSubmission;
import dynamo.backlog.tasks.files.CopyFileTask;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.backlog.tasks.files.MoveFileTask;
import dynamo.model.Downloadable;

public class FolderManager {
	
	private final static Logger LOGGER = LoggerFactory.getLogger( FolderManager.class );
	
	private Semaphore folderScanSemaphore = new Semaphore(1);
	
	DirectoryStream.Filter<Path> directoryFilter = new DirectoryStream.Filter<Path>() {
		public boolean accept(Path file) throws IOException {
			return (Files.isDirectory(file));
		}
	};

	private FolderManager() {
	}

	static class SingletonHolder {
		static FolderManager instance = new FolderManager();
	}

	public static FolderManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public static TaskSubmission moveFile( Path source, Path destinationFile, Downloadable downloadable ) {
		return BackLogProcessor.getInstance().schedule( new MoveFileTask( source, destinationFile, downloadable ), false );
	}

	public static TaskSubmission copyFile( Path source, Path destinationFile, Downloadable downloadable ) {
		return BackLogProcessor.getInstance().schedule( new CopyFileTask( source, destinationFile, downloadable ), false );
	}

	public List<Path> getContents(Path folder, Filter<Path> filter, boolean recursive) throws InterruptedException, IOException {
		List<Path> results = new ArrayList<>();
		try {
			acquire();
			try (DirectoryStream<Path> ds = filter != null ? Files.newDirectoryStream(folder, filter) : Files.newDirectoryStream(folder)) {
				for (Path p : ds) {
					if (Files.isDirectory(p) && recursive) {
						results.addAll( getContents( folder, filter, recursive) );
					} else {
						results.add( p );
					}
				}
			}
		} finally {
			release();
		}
		return results;
	}

	private void release() {
		LOGGER.debug("Releasing FolderManager Semaphore");
		folderScanSemaphore.release();
		LOGGER.debug("Released FolderManager Semaphore");
	}

	private void acquire() throws InterruptedException {
		LOGGER.debug("Acquiring FolderManager Semaphore");
		folderScanSemaphore.acquire();
		LOGGER.debug("Acquired FolderManager Semaphore");
	}

	public List<Path> getSubFolders(Path folder, boolean recursive) throws InterruptedException, IOException {			
		List<Path> subFolders = new ArrayList<>();
		try {
			acquire();
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, directoryFilter)) {
				for (Path p : ds) {
					subFolders.add( p );
					if (recursive) {
						subFolders.addAll( getSubFolders( folder, recursive) );
					}
				}
			}
		} finally {
			release();
		}
		return subFolders;
	}

	public List<Path> getAllFilesFrom( Path folder, boolean deleteEmptyFolders ) throws IOException, InterruptedException {
		List<Path> paths = new ArrayList<>();
		List<Path> contents = getInstance().getContents(folder, null, true);
		for (Path entry : contents) {
			if (Files.isDirectory(entry)) {
				List<Path> folderContents = getAllFilesFrom(entry, deleteEmptyFolders); 
				if (folderContents == null || folderContents.isEmpty()) {
					BackLogProcessor.getInstance().schedule( new DeleteTask( folder, false ));
				} else {
					paths.addAll( folderContents );
				}
			} else {
				paths.add(entry);
			}
		}
		return paths;
	}

}
