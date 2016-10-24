package dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;

import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import dynamo.tvshows.jdbi.TVShowSeasonDAO;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.backlog.RefreshTVShowTask;
import model.backlog.ScanTVShowTask;

public class RefreshTVShowFromTVDBExecutor extends TaskExecutor<RefreshTVShowTask> {
	
	private	Date nextRefreshDate = null;
	
	private TVShowSeasonDAO tvShowSeasonDAO;
	private ManagedEpisodeDAO managedEpisodeDAO;

	public RefreshTVShowFromTVDBExecutor( RefreshTVShowTask item, ManagedEpisodeDAO managedEpisodeDAO, TVShowSeasonDAO tvShowSeasonDAO ) {
		super( item );
		this.tvShowSeasonDAO = tvShowSeasonDAO;
		this.managedEpisodeDAO = managedEpisodeDAO;
	}

	@Override
	public void execute() throws TvDbException, IOException {

		Calendar calendar = Calendar.getInstance();
		calendar.add( Calendar.MONTH, 1);
		nextRefreshDate = calendar.getTime();

		ManagedSeries series = task.getSeries();

		Series tvDbSeries = TVShowManager.getInstance().getSeries( series.getId(), series.getMetaDataLanguage() != null ? series.getMetaDataLanguage() : series.getOriginalLanguage() );
		if ( tvDbSeries == null ) {
			return;
		}

		List<Episode> episodes = TVShowManager.getInstance().getAllEpisodes( series.getId(), series.getMetaDataLanguage() != null ? series.getMetaDataLanguage() : series.getOriginalLanguage() );
		if ( episodes == null ) {
			return;
		}

		series.setEnded( StringUtils.equalsIgnoreCase( tvDbSeries.getStatus(), "Ended" ));
		series.setName( tvDbSeries.getSeriesName() );	// in case of metadata language change
		
		if (StringUtils.isNotBlank( tvDbSeries.getLanguage() )) {
			// TODO
		} else if (StringUtils.equalsIgnoreCase( tvDbSeries.getNetwork(), "France 2") || StringUtils.endsWith( tvDbSeries.getNetwork(), "(FR)")) {
			series.setOriginalLanguage( Language.FR );
		}
		
		for (Episode episode : episodes) {
			
			if (episode.getSeasonNumber() < 1) {
				continue;	// ignore season 0
			}

			ManagedEpisode existingEpisode = null ;
			
			long seasonId;
			
			TVShowSeason season = tvShowSeasonDAO.findSeason( episode.getSeriesId(), episode.getSeasonNumber() );
			if ( season != null ) {
				seasonId = season.getId();
				existingEpisode = managedEpisodeDAO.findEpisode( season.getId(), episode.getEpisodeNumber() );
			} else {
				seasonId = DownloadableManager.getInstance().createDownloadable( TVShowSeason.class, String.format("%s S%02d", series.getName(), episode.getSeasonNumber()), DownloadableStatus.IGNORED );
				tvShowSeasonDAO.createSeason( seasonId, episode.getSeriesId(), episode.getSeasonNumber() );
			}

			DownloadableStatus newStatusForEpisode = series.isAutoDownload() ? DownloadableStatus.WANTED : DownloadableStatus.IGNORED;

			Date firstAiredDate = null;
			
			String firstAired = episode.getFirstAired();
			if (StringUtils.isNotBlank( firstAired )) {
				try {
					Date now = new Date();
					firstAiredDate = new SimpleDateFormat("yyyy-MM-dd").parse( firstAired );
					if (firstAiredDate.after( now )) {
						if (firstAiredDate.before( nextRefreshDate )) {
							calendar.setTime( firstAiredDate );
							calendar.add( Calendar.DAY_OF_MONTH, 1);
							nextRefreshDate = calendar.getTime();
						}
						newStatusForEpisode = DownloadableStatus.FUTURE;
					}
				} catch (ParseException e) {
					ErrorManager.getInstance().reportThrowable( task, e );
				}
			} else {
				if (!series.isEnded()) {
					newStatusForEpisode = DownloadableStatus.FUTURE;
				}
			}

			if (existingEpisode == null) {
			
				existingEpisode = new ManagedEpisode(
						DownloadableManager.getInstance().createDownloadable( ManagedEpisode.class, episode.getEpisodeName(),
								newStatusForEpisode ), newStatusForEpisode, series.getName(), null, null, null, null,
						episode.getSeriesId(), seasonId, episode.getSeasonNumber(), episode.getEpisodeNumber(), null,
						episode.getEpisodeName(), firstAiredDate, false, false, null );
			
			} else if ( existingEpisode.getStatus() == DownloadableStatus.FUTURE && newStatusForEpisode != DownloadableStatus.FUTURE) {
				
				if (newStatusForEpisode == DownloadableStatus.WANTED) {
					DownloadableManager.getInstance().want( existingEpisode );
				} else {
					DownloadableManager.getInstance().updateStatus ( existingEpisode, newStatusForEpisode );
				}
				
			} else if ( existingEpisode.getStatus() == DownloadableStatus.IGNORED && newStatusForEpisode == DownloadableStatus.FUTURE ) {
				
				DownloadableManager.getInstance().updateStatus ( existingEpisode, newStatusForEpisode );
			
			}

			if (StringUtils.isNotBlank(episode.getAbsoluteNumber())) {
				existingEpisode.setAbsoluteNumber( Integer.parseInt( episode.getAbsoluteNumber() ) );
			}

			managedEpisodeDAO.saveEpisode( existingEpisode.getId(), episode.getEpisodeNumber(), firstAiredDate, existingEpisode.getQuality(), existingEpisode.getReleaseGroup(),  
				 	existingEpisode.getSource(), existingEpisode.isSubtitled(),  existingEpisode.getSubtitlesPath(), existingEpisode.isWatched(), existingEpisode.getSeasonId() );
		}
		
		Path banner = LocalImageCache.getInstance().resolveLocal( "banners/" + series.getId() + ".jpg" );
		if ( !Files.exists(banner) || Files.size(banner) == 0 ) {
			if (tvDbSeries.getBanner() != null) {
				LocalImageCache.getInstance().download( "banners", series.getId(), tvDbSeries.getBanner(), null );
			}
		}
		Path poster = LocalImageCache.getInstance().resolveLocal( "posters/" + series.getId() + ".jpg" );
		if ( !Files.exists(poster) || Files.size(poster) == 0 ) {
			if (tvDbSeries.getBanner() != null) {
				LocalImageCache.getInstance().download( "posters", series.getId(), tvDbSeries.getPoster(), null );
			}
		}
		
		TVShowManager.getInstance().saveTVShow( series );

		if (Files.exists( series.getFolder() )) {
			queue( new ScanTVShowTask( series ), false );
		}
	}
	
	@Override
	public void rescheduleTask(RefreshTVShowTask task) {
		if ( !task.getSeries().isEnded() ) {
			task.setMinDate( nextRefreshDate );
			queue( task, false );
		}
	}

}
