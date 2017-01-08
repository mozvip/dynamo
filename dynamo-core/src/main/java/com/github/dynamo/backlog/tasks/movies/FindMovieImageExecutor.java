package com.github.dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import com.github.dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.movies.model.Movie;
import com.github.dynamo.movies.model.TheMovieDB;
import com.github.dynamo.suggesters.movies.IMDBTitle;
import com.github.dynamo.suggesters.movies.IMDBWatchListSuggester;
import com.github.dynamo.video.VideoManager;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

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
						Files.copy(localImage, imageInFolder, StandardCopyOption.REPLACE_EXISTING);
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
				MovieInfo movieDb = TheMovieDB.getInstance().getMovieInfo( movie.getMovieDbId(), null );
				if ( movieDb.getPosterPath() != null ) {
					return DownloadableManager.downloadImage(movie, TheMovieDB.getInstance().getImageURL( movieDb.getPosterPath()), null);
				}
			} catch (MovieDbException | IOException | InterruptedException e) {
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
