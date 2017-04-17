package com.github.dynamo.backlog.tasks.movies;

import java.nio.file.Path;
import java.util.List;

import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.movies.model.Movie;

public class MovieRenameDownloadableFilesExecutor extends TaskExecutor<MovieRenameDownloadableFilesTask> {

	public MovieRenameDownloadableFilesExecutor(MovieRenameDownloadableFilesTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		Movie movie = task.getDownloadable();
		List<DownloadableFile> allFiles = DownloadableManager.getInstance().getAllFiles( movie.getId() );
		
		String baseFileName = movie.toString();
		
		for (DownloadableFile downloadableFile : allFiles) {
			Path filePath = downloadableFile.getFilePath();
			String fileName = filePath.getFileName().toString();

			String extension = fileName.substring( fileName.lastIndexOf('.') );
			
			String shouldBe = baseFileName + "." + extension;
			
			if (!fileName.equalsIgnoreCase( shouldBe )) {
				// rename the file
			}
			
		}
		
		// TODO
		
	}

}
