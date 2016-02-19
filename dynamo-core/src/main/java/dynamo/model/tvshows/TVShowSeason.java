package dynamo.model.tvshows;

import java.nio.file.Path;

import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import model.ManagedSeries;

public class TVShowSeason extends Downloadable {

	private String seriesId;
	private int season;

	public TVShowSeason( Long id, DownloadableStatus status, String seriesId, String name, int seasonNumber) {
		super(id, name, null, status, null, null, null);
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
		return String.format("tvshow.jsf?id=%s#season%s", getSeriesId(), getSeason());
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
		return getSeries().getFolder().resolve( String.format(TVShowManager.getInstance().getSeasonFolderPattern(), getSeason()) );
	}

}
