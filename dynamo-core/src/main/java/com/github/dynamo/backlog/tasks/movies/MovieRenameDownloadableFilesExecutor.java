package com.github.dynamo.backlog.tasks.movies;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.movies.model.Movie;

public class MovieRenameDownloadableFilesExecutor extends TaskExecutor<MovieRenameDownloadableFilesTask> {

	private DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

	public MovieRenameDownloadableFilesExecutor(MovieRenameDownloadableFilesTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		Movie movie = task.getDownloadable();
		
		if (movie.getYear() < 0) {
			return;
		}
		
		List<DownloadableFile> allFiles = DownloadableManager.getInstance().getAllFiles( movie.getId() );
		
		String baseFileName = movie.toString();
		
		for (DownloadableFile downloadableFile : allFiles) {
			Path source = downloadableFile.getFilePath();
			String fileName = source.getFileName().toString();

			String extension = fileName.substring( fileName.lastIndexOf('.') );
			
			String shouldBe = baseFileName + extension;
			
			if (!fileName.equalsIgnoreCase( shouldBe )) {
				// rename the file
				Path target = source.getParent().resolve( shouldBe );
				if (!Files.exists( target )) {
					Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
					downloadableDAO.updatePath(source, target);
				} else {
					// ?
				}
				
			}
			
		}
		
		// TODO
		
	}

}
