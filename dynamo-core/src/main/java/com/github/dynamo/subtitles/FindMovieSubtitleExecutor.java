package com.github.dynamo.subtitles;

import java.nio.file.Path;
import java.util.Set;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.video.VideoMetaData;
import com.github.dynamo.movies.model.Movie;
import com.github.mozvip.subtitles.MovieSubtitlesFinder;
import com.github.mozvip.subtitles.RemoteSubTitles;

public class FindMovieSubtitleExecutor extends AbstractFindSubtitlesExecutor<FindMovieSubtitleTask> {

	static Set<? extends MovieSubtitlesFinder> movieSubtitlesFinders = DynamoObjectFactory
			.getInstances(MovieSubtitlesFinder.class);

	public FindMovieSubtitleExecutor(FindMovieSubtitleTask task) {
		super(task);
	}

	@Override
	public RemoteSubTitles downloadSubtitles(Path mainVideoFile, Path subtitlesFile, VideoMetaData metaData,
			Language language) {
		Movie movie = task.getMovie();
		RemoteSubTitles remoteSubTitles = null;

		for (MovieSubtitlesFinder movieSubtitlesFinder : movieSubtitlesFinders) {
			try {
				remoteSubTitles = movieSubtitlesFinder.downloadMovieSubtitles(movie.getName(), movie.getYear(),
						movie.getReleaseGroup(), metaData.getFps(), task.getSubtitlesLanguage().getLocale());
				if (remoteSubTitles != null) {
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		return remoteSubTitles;
	}

	@Override
	public Language getSubtitlesLanguage() {
		return task.getSubtitlesLanguage();
	}

}
