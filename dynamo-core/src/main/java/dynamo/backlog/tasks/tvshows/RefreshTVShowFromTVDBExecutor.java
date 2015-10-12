package dynamo.backlog.tasks.tvshows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;

import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.TVShowDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.backlog.RefreshTVShowTask;
import model.backlog.ScanTVShowTask;

public class RefreshTVShowFromTVDBExecutor extends TaskExecutor<RefreshTVShowTask> {
	
	private	Date nextRefreshDate = null;
	
	private TVShowDAO tvShowDAO;

	public RefreshTVShowFromTVDBExecutor( RefreshTVShowTask item, TVShowDAO tvShowDAO ) {
		super( item );
		this.tvShowDAO = tvShowDAO;
	}

	@Override
	public void execute() {

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
		
		for (Episode episode : episodes) {
			
			if (episode.getSeasonNumber() < 1) {
				continue;	// ignore season 0
			}

			ManagedEpisode existingEpisode = null ;
			
			long seasonId;
			
			TVShowSeason season = tvShowDAO.findSeason( episode.getSeriesId(), episode.getSeasonNumber() );
			if ( season != null ) {
				seasonId = season.getId();
				existingEpisode = tvShowDAO.findEpisode( season.getId(), episode.getEpisodeNumber() );
			} else {
				Path seasonPath = series.getFolder().resolve( String.format( TVShowManager.getInstance().getSeasonFolderPattern(), episode.getSeasonNumber()));
				seasonId = DownloadableManager.getInstance().createDownloadable( TVShowSeason.class, seasonPath, null, DownloadableStatus.IGNORED );
				tvShowDAO.createSeason( seasonId, episode.getSeriesId(), episode.getSeasonNumber() );
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
			
				existingEpisode = new ManagedEpisode( DownloadableManager.getInstance().createDownloadable( ManagedEpisode.class, null, null, newStatusForEpisode ), null, newStatusForEpisode, series.getName(), null, episode.getId(), null, null, null, episode.getSeriesId(), seasonId, episode.getSeasonNumber(), episode.getEpisodeNumber(), null, episode.getEpisodeName(), firstAiredDate, false, false );
			
			} else if ( existingEpisode.getStatus() == DownloadableStatus.FUTURE && newStatusForEpisode != DownloadableStatus.FUTURE) {
				
				if (newStatusForEpisode == DownloadableStatus.WANTED) {
					DownloadableManager.getInstance().want( existingEpisode );
				} else {
					DownloadableManager.getInstance().updateStatus ( existingEpisode.getId(), newStatusForEpisode );
				}
				
			} else if ( existingEpisode.getStatus() == DownloadableStatus.IGNORED && newStatusForEpisode == DownloadableStatus.FUTURE ) {
				
				DownloadableManager.getInstance().updateStatus ( existingEpisode.getId(), newStatusForEpisode );
			
			}

			existingEpisode.setFirstAired( firstAiredDate );
			existingEpisode.setEpisodeName( episode.getEpisodeName() );
			existingEpisode.setEpisodeNumber( episode.getEpisodeNumber() );

			if (StringUtils.isNotBlank(episode.getAbsoluteNumber())) {
				existingEpisode.setAbsoluteNumber( Integer.parseInt( episode.getAbsoluteNumber() ) );
			}

			tvShowDAO.saveEpisode( existingEpisode, existingEpisode.getQuality(), existingEpisode.getSource(), existingEpisode.getSubtitlesPath() );
		}
		
		if ( ( series.getBanner() == null && tvDbSeries.getBanner() != null ) || LocalImageCache.getInstance().missFile( series.getBanner() ) ) {
			series.setBanner( LocalImageCache.getInstance().download( "banners", tvDbSeries.getSeriesName(), tvDbSeries.getBanner(), null ) );
		}

		if ( ( series.getPoster() == null && tvDbSeries.getPoster() != null ) || LocalImageCache.getInstance().missFile( series.getPoster() ) ) {
			series.setPoster( LocalImageCache.getInstance().download( "posters", tvDbSeries.getSeriesName(), tvDbSeries.getPoster(), null ) );
		}
		
		tvShowDAO.saveTVShow(
				series, series.getMetaDataLanguage(), series.getOriginalLanguage(), series.getAudioLanguage(), series.getSubtitleLanguage(), series.getFolder(),
				series.getWordsBlackList(), series.getAka(), TVShowManager.getInstance().getTvShowQualities() );

		if (Files.exists( series.getFolder() )) {
			queue( new ScanTVShowTask( series ));
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
