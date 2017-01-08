package com.github.dynamo.model.backlog.subtitles;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.movies.model.Movie;

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
	public String toString() {
		return String.format("Find subtitles for <a href='%s'>%s</a> in %s", movie.getRelativeLink(), movie.toString(), subtitlesLanguage.getLabel());
	}
}
