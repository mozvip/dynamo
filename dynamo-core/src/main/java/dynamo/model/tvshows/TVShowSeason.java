package dynamo.model.tvshows;

import java.nio.file.Path;

import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import model.ManagedSeries;

public class TVShowSeason extends Downloadable {

	private String seriesId;
	private String seriesName;
	private int season;

	public TVShowSeason( Long id, DownloadableStatus status, Path path, String seriesId, String seriesName, int seasonNumber) {
		super(id, status, path, null, null, null);
		this.seriesId = seriesId;
		this.seriesName = seriesName;
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
		if (season >= 0) {
			return String.format("%s season %d", seriesName, season);
		} else {
			return String.format("%s", seriesName);
		}
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
	public Path getDestinationFolder() {
		return getSeries().getFolder().resolve( String.format(TVShowManager.getInstance().getSeasonFolderPattern(), getSeason()) );
	}

}
