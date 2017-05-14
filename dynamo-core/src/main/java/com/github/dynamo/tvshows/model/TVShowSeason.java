package com.github.dynamo.tvshows.model;

import java.nio.file.Path;

import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;

public class TVShowSeason extends Downloadable {

	private String seriesId;
	private int season;

	public TVShowSeason( Long id, DownloadableStatus status, String seriesId, String name, int seasonNumber) {
		super(id, name, null, status, null, -1, null);
		this.seriesId = seriesId;
		this.season = seasonNumber;
	}

	public int getSeason() {
		return season;
	}

	public String getSeriesId() {
		return seriesId;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getRelativeLink() {
		return String.format( "/tvshow-detail/%s#season%d", seriesId, season ); 
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
		return getSeries().getFolder().resolve( String.format("Season %02d", getSeason()) );
	}

}
