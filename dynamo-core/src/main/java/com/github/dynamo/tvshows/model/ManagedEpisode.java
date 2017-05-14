package com.github.dynamo.tvshows.model;

import java.nio.file.Path;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.VideoSource;
import com.github.dynamo.core.jackson.LocalDateSerializer;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.Video;

public class ManagedEpisode extends Downloadable implements Video {

	private VideoQuality quality;
	private VideoSource source;
	private String releaseGroup;

	private String seriesId;
	private long seasonId;

	private int seasonNumber;
	private int episodeNumber;
	private Integer absoluteNumber;

	@JsonSerialize(using=LocalDateSerializer.class)
	private LocalDate firstAired;

	private boolean watched = false;
	
	private String seriesName;

	public ManagedEpisode(Long id, DownloadableStatus status, String seriesName,
			VideoQuality quality, VideoSource source,
			String releaseGroup, String seriesId, long seasonId, int seasonNumber,
			int episodeNumber, Integer absoluteNumber, String episodeName,
			LocalDate firstAired, boolean watched, String label) {

		super(id, episodeName, label, status, null, -1, null);
		
		this.seriesName = seriesName;
		this.quality = quality;
		this.source = source;
		this.releaseGroup = releaseGroup;
		this.seriesId = seriesId;
		this.seasonNumber = seasonNumber;
		this.seasonId = seasonId;
		this.episodeNumber = episodeNumber;
		this.absoluteNumber = absoluteNumber;
		this.firstAired = firstAired;
		this.watched = watched;
	}
	
	public Integer getAbsoluteNumber() {
		return absoluteNumber;
	}
	
	public void setAbsoluteNumber(Integer absoluteNumber) {
		this.absoluteNumber = absoluteNumber;
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

	public int getEpisodeNumber() {
		return episodeNumber;
	}

	public void setEpisodeNumber(int episodeNumber) {
		this.episodeNumber = episodeNumber;
	}

	public LocalDate getFirstAired() {
		return firstAired;
	}

	public void setFirstAired(LocalDate firstAired) {
		this.firstAired = firstAired;
	}

	public boolean isAired() {
		return !getStatus().equals( DownloadableStatus.FUTURE );
	}

	@Override
	public boolean isWatched() {
		return watched;
	}

	@Override
	public void setWatched(boolean watched) {
		this.watched = watched;
	}

	public long getSeasonId() {
		return seasonId;
	}

	public int getSeasonNumber() {
		return seasonNumber;
	}
	
	public String getSeriesId() {
		return seriesId;
	}

	@Override
	public String toString() {
		if ( seasonNumber < 0 ) {
			return String.format("%s %d", seriesName, episodeNumber );
		} else {
			return String.format("%s S%02dE%02d", seriesName, seasonNumber, episodeNumber );
		}
	}

	@Override
	public String getRelativeLink() {
		return String.format( "/tvshow-detail/%s#season%d", seriesId, seasonNumber ); 
	}
	
	private ManagedSeries series = null;
	public ManagedSeries getSeries() {
		if (series == null) {
			series = TVShowManager.getInstance().getManagedSeries( getSeriesId() );	
		}
		return series;
	}
	
	@Override
	public Path determineDestinationFolder() {
		return getSeries().getFolder().resolve( String.format("Season %02d", getSeasonNumber()) );
	}

}
