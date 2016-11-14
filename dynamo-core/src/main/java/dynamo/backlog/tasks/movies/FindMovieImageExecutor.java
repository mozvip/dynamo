package dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.DownloadableManager;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;
import dynamo.video.VideoManager;

public class FindMovieImageExecutor extends FindDownloadableImageExecutor<Movie> {

	public FindMovieImageExecutor(FindMovieImageTask task) {
		super(task);
	}
	
	@Override
	public void onImageFound( Path localImage ) {
		
		Movie movie = task.getDownloadable();
		
		if (movie.isDownloaded()) {
			Optional<Path> mainVideoFile = VideoManager.getInstance().getMainVideoFile( movie.getId() );
			if (mainVideoFile.isPresent()) {
				Path imageInFolder = getImageInFolderPath(mainVideoFile.get());
				if (!Files.exists(imageInFolder)) {
					try {
						Files.copy( localImage, imageInFolder);
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}
		}
	}

	private Path getImageInFolderPath(Path mainVideoFile) {
		String path = mainVideoFile.toAbsolutePath().toString();
		Path imageInFolder = Paths.get( path.substring(0, path.lastIndexOf('.')) + ".jpg" );
		return imageInFolder;
	}	

	@Override
	public boolean downloadImageTo(Path localImage) {

		Movie movie = task.getDownloadable();
		
		if (movie.isDownloaded()) {
			Optional<Path> mainVideoFile = VideoManager.getInstance().getMainVideoFile( movie.getId() );
			if (mainVideoFile.isPresent()) {
				Path imageInFolder = getImageInFolderPath(mainVideoFile.get());
				if (Files.exists( imageInFolder )) {
					try {
						return DownloadableManager.downloadImage( movie, imageInFolder );
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}		
			}
		}

		// try from MovieDB first
		if (movie.getMovieDbId() > 0) {
			try {
				MovieInfo movieDb = MovieManager.getInstance().getMovieInfo( movie.getMovieDbId() );
				if ( movieDb.getPosterPath() != null ) {
					return DownloadableManager.downloadImage(movie, MovieManager.getInstance().getImageURL( movieDb.getPosterPath()), null);
				}
			} catch (MovieDbException | IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		// and then from IMDB
		if (movie.getImdbID() != null) {
			try {
				IMDBTitle imdbData = IMDBWatchListSuggester.extractIMDBTitle( movie.getImdbID() );
				if (imdbData.getImage() != null) {
					return DownloadableManager.downloadImage(movie, imdbData.getImage().getUrl(), imdbData.getImage().getReferer());
				}
				return true;
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		return false;
	}

}
