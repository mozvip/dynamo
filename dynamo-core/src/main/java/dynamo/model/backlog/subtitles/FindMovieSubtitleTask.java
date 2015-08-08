package dynamo.model.backlog.subtitles;

import dynamo.core.Language;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.Task;
import dynamo.model.movies.Movie;

public class FindMovieSubtitleTask extends Task {
	
	private Movie movie;
	private Language subtitlesLanguage;

	public FindMovieSubtitleTask( Movie movie, Language subtitlesLanguage ) {
		this.movie = movie;
		this.subtitlesLanguage = subtitlesLanguage;
	}

	public Language getSubtitlesLanguage() {
		return subtitlesLanguage;
	}

	public void setSubtitlesLanguage(Language subtitlesLanguage) {
		this.subtitlesLanguage = subtitlesLanguage;
	}

	public Movie getMovie() {
		return movie;
	}
	
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	
	
	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return FindSubtitlesQueue.class;
	}

	@Override
	public String toString() {
		return String.format("Find subtitles for <a href='%s'>%s</a> in %s", movie.getRelativeLink(), movie.toString(), subtitlesLanguage.getFullName());
	}
}
