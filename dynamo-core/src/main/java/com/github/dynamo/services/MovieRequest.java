package com.github.dynamo.services;

import com.github.dynamo.core.Language;

public class MovieRequest {
	
	private int movieDbId;
	private Language audioLanguage;
	private Language subtitlesLanguage;

	public int getMovieDbId() {
		return movieDbId;
	}
	public void setMovieDbId(int movieDbId) {
		this.movieDbId = movieDbId;
	}
	public Language getAudioLanguage() {
		return audioLanguage;
	}
	public void setAudioLanguage(Language audioLanguage) {
		this.audioLanguage = audioLanguage;
	}
	public Language getSubtitlesLanguage() {
		return subtitlesLanguage;
	}
	public void setSubtitlesLanguage(Language subtitlesLanguage) {
		this.subtitlesLanguage = subtitlesLanguage;
	}

}
