package dynamo.backlog.tasks.files;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import dynamo.core.manager.ErrorManager;

public class FileUtils {

	public static Path getFolderWithMostUsableSpace(List<Path> folders) {
		
		if (folders.size() == 1) {
			return folders.get(0);
		}
		
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

	public static boolean isDirEmpty(final Path directory) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		}
	}

}
