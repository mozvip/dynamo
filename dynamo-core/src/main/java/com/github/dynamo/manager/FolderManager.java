package com.github.dynamo.manager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.TaskSubmission;
import com.github.dynamo.backlog.tasks.files.CopyFileTask;
import com.github.dynamo.backlog.tasks.files.DeleteEvent;
import com.github.dynamo.backlog.tasks.files.MoveFileTask;
import com.github.dynamo.model.Downloadable;

public class FolderManager {
	
	private final static Logger LOGGER = LoggerFactory.getLogger( FolderManager.class );
	
	private Semaphore folderScanSemaphore = new Semaphore(2);
	
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
	
	private List<Path> internal_getContents(Path folder, Filter<Path> filter, boolean recursive) throws InterruptedException, IOException {
		List<Path> results = new ArrayList<>();
		List<Path> folderResults = new ArrayList<>();
		try (DirectoryStream<Path> ds = filter != null ? Files.newDirectoryStream(folder, filter) : Files.newDirectoryStream(folder)) {
			for (Path p : ds) {
				folderResults.add( p );
			}
		}
		for (Path p : folderResults) {
			if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS) && recursive) {
				results.addAll( internal_getContents( p, filter, recursive) );
			} else {
				results.add( p );
			}
		}
		return results;
	}

	public List<Path> getContents(Path folder, Filter<Path> filter, boolean recursive) throws InterruptedException, IOException {
		try {
			acquire();
			return internal_getContents( folder, filter, recursive );
		} finally {
			release();
		}
	}

	private void release() {
		folderScanSemaphore.release();
	}

	private void acquire() throws InterruptedException {
		folderScanSemaphore.acquire();
	}

	public List<Path> getSubFolders(Path folder, boolean recursive) throws InterruptedException, IOException {			
		try {
			acquire();
			return internal_getSubFolders(folder, recursive);
		} finally {
			release();
		}
	}

	private List<Path> internal_getSubFolders(Path folder, boolean recursive) throws IOException {
		List<Path> subFolders = new ArrayList<>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, directoryFilter)) {
			for (Path p : ds) {
				subFolders.add( p );
				if (recursive) {
					subFolders.addAll( internal_getSubFolders( folder, recursive) );
				}
			}
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
					BackLogProcessor.getInstance().post( new DeleteEvent( folder, false ));
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
