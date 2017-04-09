package com.github.dynamo.subtitles;

import com.github.dynamo.core.Language;
import com.github.dynamo.movies.model.Movie;

public class FindMovieSubtitleTask extends AbstractFindSubtitlesTask {
	
	private Movie movie;
	private Language subtitlesLanguage;

	public FindMovieSubtitleTask( Movie movie, Language subtitlesLanguage ) {
		super(movie);
		this.movie = movie;
		this.subtitlesLanguage = subtitlesLanguage;
	}

	public Movie getMovie() {
		return movie;
	}
	
	public Language getSubtitlesLanguage() {
		return subtitlesLanguage;
	}

	@Override
	public String toString() {
		return String.format("Find subtitles for <a href='%s'>%s</a> in %s", movie.getRelativeLink(), movie.toString(), subtitlesLanguage.getLabel());
	}
}
