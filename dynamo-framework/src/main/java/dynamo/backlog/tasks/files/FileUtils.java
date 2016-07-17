package dynamo.backlog.tasks.files;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dynamo.core.manager.ErrorManager;

public class FileUtils {

	public static Path getFolderWithMostUsableSpace(Collection<Path> folders) {
		Path destinationFolder = Paths.get(".");
		long currentFreeSpace = 0;
		for (Path folder : folders) {
			long freeSpace;
			try {
				if (!Files.exists(folder)) {
					Files.createDirectories(folder);
				}
				freeSpace = Files.getFileStore(folder).getUsableSpace();
				if (freeSpace > currentFreeSpace) {
					currentFreeSpace = freeSpace;
					destinationFolder = folder;
				}
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable(e);
			}
		}
		return destinationFolder;
	}

	public static List<Path> getChildFolders(Path folder) throws IOException {
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			public boolean accept(Path file) throws IOException {
				return (Files.isDirectory(file));
			}
		};
		
		List<Path> results = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, filter)) {
			for (Path entry : stream) {
				results.add( entry );
			}
		}
		
		return results;
	}

	public static boolean isDirEmpty(final Path directory) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		}
	}

}
