package model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Date;

import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.Video;
import dynamo.model.tvshows.TVShowManager;

public class ManagedEpisode extends Downloadable implements Video {

	private Path subtitlesPath;

	private VideoQuality quality;
	private VideoSource source;
	private String releaseGroup;

	private String seriesId;
	private long seasonId;

	private int seasonNumber;
	private int episodeNumber;
	private Integer absoluteNumber;

	private LocalDate firstAired;

	private boolean subtitled = false;
	private boolean watched = false;
	
	private String seriesName;

	public ManagedEpisode(Long id, DownloadableStatus status, String seriesName, Path subtitlesPath,
			VideoQuality quality, VideoSource source,
			String releaseGroup, String seriesId, long seasonId, int seasonNumber,
			int episodeNumber, Integer absoluteNumber, String episodeName,
			LocalDate firstAired, boolean subtitled, boolean watched, String label) {

		super(id, episodeName, label, status, null, -1, null);
		
		this.seriesName = seriesName;
		this.subtitlesPath = subtitlesPath;
		this.quality = quality;
		this.source = source;
		this.releaseGroup = releaseGroup;
		this.seriesId = seriesId;
		this.seasonNumber = seasonNumber;
		this.seasonId = seasonId;
		this.episodeNumber = episodeNumber;
		this.absoluteNumber = absoluteNumber;
		this.firstAired = firstAired;
		this.subtitled = subtitled;
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
	public Path getSubtitlesPath() {
		return subtitlesPath;
	}

	@Override
	public void setSubtitlesPath(Path subtitlesPath) {
		this.subtitlesPath = subtitlesPath;
		if ( subtitlesPath != null && Files.exists( subtitlesPath ) ) {
			subtitled = true;
		}
	}

	@Override
	public boolean isSubtitled() {
		return subtitled;
	}

	@Override
	public void setSubtitled(boolean subtitled) {
		this.subtitled = subtitled;
		if (!subtitled) {
			subtitlesPath = null;
		}
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
		return String.format( "index.html#/tvshow-detail/%s#season%d", seriesId, seasonNumber ); 
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
