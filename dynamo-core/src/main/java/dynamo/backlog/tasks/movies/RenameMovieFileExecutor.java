package dynamo.backlog.tasks.movies;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import core.RegExp;
import dynamo.backlog.tasks.core.SubtitlesFileFilter;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.movies.jdbi.MovieDAO;
import dynamo.movies.model.Movie;
import dynamo.video.VideoManager;

public class RenameMovieFileExecutor extends TaskExecutor< RenameMovieFileTask > {
	
	private static MovieDAO movieDAO = DAOManager.getInstance().getDAO( MovieDAO.class );
	
	public RenameMovieFileExecutor(RenameMovieFileTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		Movie movie = task.getMovie();
		
		if (movie.getMovieDbId() == -1 || movie.getImdbID() == null) {
			// it is highly probable the movie was not properly identified, so do not take the risk to rename it
			return;
		}
		
		Optional<Path> mainVideoFile = VideoManager.getInstance().getMainVideoFile( movie.getId() );
		if (!mainVideoFile.isPresent()) {
			return;
		}

		String mainVideoFileName = mainVideoFile.get().getFileName().toString();
		String currentName = RegExp.extract( mainVideoFileName, "(.*)\\.\\w+");
		String newFileName = String.format("%s (%d)", movie.getName(), movie.getYear() );	// MAYBE_TODO : externalize renaming pattern ?

		if ( currentName.equals( newFileName )) {
			return;
		}

		newFileName = newFileName.replace(':', '-');
		newFileName = newFileName.replace("?", "");

		List<DownloadableFile> allFiles = DownloadableManager.getInstance().getAllFiles( movie.getId() );
		for (DownloadableFile downloadableFile : allFiles) {
			
			boolean videoFile = VideoFileFilter.getInstance().accept( downloadableFile.getFilePath() );
			boolean subtitlesFile = SubtitlesFileFilter.getInstance().accept( downloadableFile.getFilePath() ); 
			String fileName = downloadableFile.getFilePath().getFileName().toString();
			String fileExtension = fileName.substring( fileName.lastIndexOf('.') );
			Path newFilePath = downloadableFile.getFilePath().getParent().resolve( newFileName + fileExtension );
			Files.move( downloadableFile.getFilePath(), newFilePath, StandardCopyOption.REPLACE_EXISTING );
			if (subtitlesFile) {
				movieDAO.setSubtitled(movie.getId(), newFilePath);
			}

		}
	}

}
