package dynamo.manager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.CopyFileTask;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.backlog.tasks.files.MoveFileTask;
import dynamo.core.model.Task;
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
	
	public static Task moveFile( Path source, Path destinationFile, Downloadable downloadable ) {
		return BackLogProcessor.getInstance().schedule( new MoveFileTask( source, destinationFile, downloadable ), false );
	}

	public static Task copyFile( Path source, Path destinationFile, Downloadable downloadable ) {
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
	
	public static void moveAssociatedFiles( Path mainFilePath, Path destinationFolder, Downloadable downloadable ) throws IOException {
		
		// FIXME: move additional files like subtitles, ... which may have a different name			

		// move associated files ( all files with same name and different extension )
		List<Path> associatedFiles = getAssociatedFiles( mainFilePath ); 
		for (Path path : associatedFiles) {
			moveFile( path, destinationFolder.resolve( path.getFileName().toString() ), downloadable);
		}
	}
	
	public static List<Path> getAssociatedFiles( Path mainFilePath ) throws IOException {
		
		List<Path> associatedFiles = new ArrayList<Path>();
		
		String fileNameWithoutExtension = mainFilePath.getFileName().toString();
		fileNameWithoutExtension = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.lastIndexOf('.'));
		try (DirectoryStream<Path> ds = Files.newDirectoryStream( mainFilePath.getParent() )) {
			for (Path entry : ds) {
				if (entry.equals( mainFilePath )) {
					continue;
				}
				String entryFilename = entry.getFileName().toString();
				if (entryFilename.startsWith( fileNameWithoutExtension )) {
					associatedFiles.add( entry );
				}
			}
		}
		
		return associatedFiles;
	}

}
