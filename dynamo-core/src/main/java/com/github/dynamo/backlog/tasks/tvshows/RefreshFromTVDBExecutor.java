package com.github.dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.LocalImageCache;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import com.github.dynamo.tvshows.jdbi.TVShowSeasonDAO;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.github.dynamo.tvshows.model.TVShowSeason;
import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;

public class RefreshFromTVDBExecutor extends TaskExecutor<RefreshFromTVDBTask> {
	
	private	LocalDateTime nextRefreshDate = null;
	
	private TVShowSeasonDAO tvShowSeasonDAO = DAOManager.getInstance().getDAO(TVShowSeasonDAO.class);
	private ManagedEpisodeDAO managedEpisodeDAO = DAOManager.getInstance().getDAO(ManagedEpisodeDAO.class);
	private DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO(DownloadableUtilsDAO.class);

	public RefreshFromTVDBExecutor( RefreshFromTVDBTask item ) {
		super( item );
	}

	@Override
	public void execute() throws TvDbException, IOException {

		nextRefreshDate = LocalDateTime.now();
		nextRefreshDate = nextRefreshDate.plusMonths( 1 );

		ManagedSeries series = task.getSeries();

		Language metaDataLanguage = series.getMetaDataLanguage() != null ? series.getMetaDataLanguage() : series.getOriginalLanguage();
		Series tvDbSeries = TVShowManager.getInstance().getSeries( series.getId(), metaDataLanguage );
		if ( tvDbSeries == null ) {
			return;
		}

		List<Episode> episodes = TVShowManager.getInstance().getAllEpisodes( series.getId(), metaDataLanguage );
		if ( episodes == null ) {
			return;
		}

		series.setEnded( StringUtils.equalsIgnoreCase( tvDbSeries.getStatus(), "Ended" ));
		series.setName( tvDbSeries.getSeriesName() );	// in case of metadata language change
		
		if (StringUtils.isNotBlank( tvDbSeries.getLanguage() )) {
			// TODO
		} else if (StringUtils.equalsIgnoreCase( tvDbSeries.getNetwork(), "La Une") || StringUtils.equalsIgnoreCase( tvDbSeries.getNetwork(), "France 2") || StringUtils.endsWith( tvDbSeries.getNetwork(), "(FR)")) {
			series.setOriginalLanguage( Language.FR );
		}
		
		TVShowSeason currentSeason = null;	
		
		for (Episode episode : episodes) {
			
			if (episode.getSeasonNumber() < 1) {
				continue;	// ignore season 0
			}

			ManagedEpisode existingEpisode = null ;
			
			long seasonId;
			
			LocalDate firstAiredDate = null;
			if (StringUtils.isNotBlank( episode.getFirstAired() )) {
				firstAiredDate = LocalDate.parse(episode.getFirstAired(), DateTimeFormatter.ofPattern("yyyy-MM-dd") );
			}
			
			if (currentSeason == null || currentSeason.getSeason() != episode.getSeasonNumber()) {
				currentSeason = tvShowSeasonDAO.findSeason( episode.getSeriesId(), episode.getSeasonNumber() );
			}
			if ( currentSeason != null ) {
				seasonId = currentSeason.getId();
				existingEpisode = managedEpisodeDAO.findEpisode( currentSeason.getId(), episode.getEpisodeNumber() );
			} else {
				seasonId = DownloadableManager.getInstance().createDownloadable( TVShowSeason.class, String.format("%s S%02d", series.getName(), episode.getSeasonNumber()), firstAiredDate != null ? firstAiredDate.getYear() : -1, DownloadableStatus.IGNORED );
				tvShowSeasonDAO.createSeason( seasonId, episode.getSeriesId(), episode.getSeasonNumber() );
			}

			DownloadableStatus newStatusForEpisode = series.isAutoDownload() ? DownloadableStatus.WANTED : DownloadableStatus.IGNORED;
			
			int year = -1;
			if (firstAiredDate != null) {
				year = firstAiredDate.getYear();
				LocalDate now = LocalDate.now();
				if (firstAiredDate.isAfter( now )) {
					if (firstAiredDate.isBefore( nextRefreshDate.toLocalDate() )) {
						nextRefreshDate.plusMonths(1);
					}
					newStatusForEpisode = DownloadableStatus.FUTURE;
				}
			} else {
				if (!series.isEnded()) {
					newStatusForEpisode = DownloadableStatus.FUTURE;
				}
			}

			if (existingEpisode == null) {
			
				existingEpisode = new ManagedEpisode(
						DownloadableManager.getInstance().createDownloadable( ManagedEpisode.class, episode.getEpisodeName(), year, newStatusForEpisode ),
						newStatusForEpisode, series.getName(), null, null, null, episode.getSeriesId(), seasonId,
						episode.getSeasonNumber(), episode.getEpisodeNumber(), null,
						episode.getEpisodeName(), firstAiredDate, false, null );
			
			} else if ( existingEpisode.getStatus() == DownloadableStatus.FUTURE && newStatusForEpisode != DownloadableStatus.FUTURE) {
				
				if (newStatusForEpisode == DownloadableStatus.WANTED) {
					DownloadableManager.getInstance().want( existingEpisode );
				} else {
					DownloadableManager.getInstance().updateStatus ( existingEpisode, newStatusForEpisode );
				}
				
			} else if ( newStatusForEpisode == DownloadableStatus.FUTURE ) {
				
				DownloadableManager.getInstance().updateStatus ( existingEpisode, newStatusForEpisode );
			
			}

			if (StringUtils.isNotBlank(episode.getAbsoluteNumber())) {
				existingEpisode.setAbsoluteNumber( Integer.parseInt( episode.getAbsoluteNumber() ) );
			}

			managedEpisodeDAO.saveEpisode(
					existingEpisode.getId(), episode.getEpisodeNumber(), firstAiredDate, existingEpisode.getQuality(), existingEpisode.getReleaseGroup(),  
				 	existingEpisode.getSource(), existingEpisode.isWatched(), existingEpisode.getSeasonId() );
			downloadableDAO.updateName( existingEpisode.getId(), episode.getEpisodeName());
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
			BackLogProcessor.getInstance().schedule( new ScanTVShowTask( series ), false );
		}
	}
	
	@Override
	public void rescheduleTask(RefreshFromTVDBTask task) {
		if ( !task.getSeries().isEnded() ) {
			BackLogProcessor.getInstance().schedule( task, nextRefreshDate, false );
		}
	}

}
