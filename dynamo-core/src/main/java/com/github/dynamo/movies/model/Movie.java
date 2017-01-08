package com.github.dynamo.movies.model;

import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.files.FileUtils;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.VideoSource;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.Video;

public class Movie extends Downloadable implements Video {

	private VideoQuality wantedQuality;
	private Language wantedAudioLanguage;
	private Language wantedSubtitlesLanguage;
	private Language originalLanguage;
	
	private VideoQuality quality;
	private VideoSource source;
	private String releaseGroup;

	private int movieDbId;
	private String imdbID;
	private String traktUrl;
	
	private float rating;
	
	private boolean watched;

	public Movie( Long id, DownloadableStatus status, String aka, String name, String label,
			VideoQuality wantedQuality, Language wantedAudioLanguage,
			Language wantedSubtitlesLanguage, Language originalLanguage,
			VideoQuality quality, VideoSource source,
			String releaseGroup, int movieDbId,
			String imdbID, String traktUrl, float rating, int year,
			boolean watched) {
		super( id, name, label, status, aka, year, null );

		this.wantedQuality = wantedQuality;
		this.wantedAudioLanguage = wantedAudioLanguage;
		this.wantedSubtitlesLanguage = wantedSubtitlesLanguage;
		this.originalLanguage = originalLanguage;
		this.quality = quality;
		this.source = source;
		this.releaseGroup = releaseGroup;
		this.movieDbId = movieDbId;
		this.imdbID = imdbID;
		this.traktUrl = traktUrl;
		this.rating = rating;
		this.watched = watched;
	}

	public VideoQuality getQuality() {
		return quality;
	}

	public void setQuality(VideoQuality quality) {
		this.quality = quality;
	}

	public VideoSource getSource() {
		return source;
	}

	public void setSource(VideoSource source) {
		this.source = source;
	}

	public String getReleaseGroup() {
		return releaseGroup;
	}

	public void setReleaseGroup(String releaseGroup) {
		this.releaseGroup = releaseGroup;
	}

	public int getMovieDbId() {
		return movieDbId;
	}

	public void setMovieDbId(int movieDbId) {
		this.movieDbId = movieDbId;
	}

	public String getImdbID() {
		return imdbID;
	}

	public void setImdbID(String imdbID) {
		this.imdbID = imdbID;
	}
	
	public float getRating() {
		return rating;
	}
	
	public void setRating(float rating) {
		this.rating = rating;
	}

	public VideoQuality getWantedQuality() {
		return wantedQuality != null ? wantedQuality : VideoQuality._1080p;	// hack for bad data : shouldn't happen ?
	}

	public void setWantedQuality(VideoQuality wantedQuality) {
		this.wantedQuality = wantedQuality;
	}

	public Language getWantedAudioLanguage() {
		return wantedAudioLanguage;
	}

	public void setWantedAudioLanguage(Language wantedAudioLanguage) {
		this.wantedAudioLanguage = wantedAudioLanguage;
	}

	public Language getWantedSubtitlesLanguage() {
		return wantedSubtitlesLanguage;
	}

	public void setWantedSubtitlesLanguage(Language wantedSubtitlesLanguage) {
		this.wantedSubtitlesLanguage = wantedSubtitlesLanguage;
	}

	public Language getOriginalLanguage() {
		return originalLanguage;
	}

	public String getTraktUrl() {
		return traktUrl;
	}

	public void setTraktUrl(String traktUrl) {
		this.traktUrl = traktUrl;
	}
	
	@Override
	public boolean isWatched() {
		return watched;
	}

	@Override
	public void setWatched(boolean watched) {
		this.watched = watched;
	}

	@Override
	public String getRelativeLink() {
		return "index.html#/movies/" + getStatus().name();
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals( other.toString() );
	}

	@Override
	public String toString() {
		return String.format("%s (%d)", getName(), year);
	}
	
	@Override
	public Path determineDestinationFolder() {
		return FileUtils.getFolderWithMostUsableSpace(MovieManager.getInstance().getFolders());
	}
	
}
