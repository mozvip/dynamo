package dynamo.manager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.TaskSubmission;
import dynamo.backlog.tasks.files.CopyFileTask;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.backlog.tasks.files.MoveFileTask;
import dynamo.model.Downloadable;

public class FolderManager {

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
	
	public static List<Path> getAllFilesFrom( Path folder, boolean deleteEmptyFolders ) throws IOException {
		List<Path> paths = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream( folder )) {
			for (Path entry: stream) {
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
		}
		return paths;
	}

}
