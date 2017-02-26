package com.github.dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.ScanFolderExecutor;
import com.github.dynamo.backlog.tasks.core.VideoFileFilter;
import com.github.dynamo.backlog.tasks.files.DeleteDownloadableEvent;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.FolderManager;
import com.github.dynamo.model.backlog.find.FindEpisodeTask;
import com.github.dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import com.github.dynamo.parsers.TVShowEpisodeInfo;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import com.github.dynamo.tvshows.jdbi.TVShowSeasonDAO;
import com.github.dynamo.tvshows.jdbi.UnrecognizedDAO;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.github.dynamo.tvshows.model.TVShowSeason;
import com.github.dynamo.video.VideoManager;
import com.github.mozvip.subtitles.Release;

public class ScanTVShowExecutor extends ScanFolderExecutor<ScanTVShowTask> {
	
	private TVShowSeasonDAO tvShowSeasonDAO;
	private DownloadableUtilsDAO downloadableDAO;
	private ManagedEpisodeDAO managedEpisodeDAO;
	private UnrecognizedDAO unrecognizedDAO;

	public ScanTVShowExecutor(ScanTVShowTask task, TVShowSeasonDAO tvShowSeasonDAO, DownloadableUtilsDAO downloadableDAO, ManagedEpisodeDAO managedEpisodeDAO, UnrecognizedDAO unrecognizedDAO) {
		super(task);
		this.tvShowSeasonDAO = tvShowSeasonDAO;
		this.downloadableDAO = downloadableDAO;
		this.managedEpisodeDAO = managedEpisodeDAO;
		this.unrecognizedDAO = unrecognizedDAO;
	}

	private void parseFolder( ManagedSeries series, List<TVShowSeason> seasons, List<ManagedEpisode> existingEpisodes, Path folder ) throws IOException, InterruptedException {
		
		List<Path> videoFiles = FolderManager.getInstance().getContents(folder, VideoFileFilter.getInstance(), true);
		for (Path p : videoFiles) {

			DownloadableFile downloadableFile = DownloadableManager.getInstance().getFile( p );
			if (downloadableFile != null) {
				continue;
			}

			int seasonNumber = -1;
			List<Integer> episodes = new ArrayList<Integer>();
			
			TVShowEpisodeInfo episodeInfo = VideoNameParser.getTVShowEpisodeInfo(series, p);
			
			boolean episodeInfoFound = false;
			
			if ( episodeInfo != null ) {
				seasonNumber = episodeInfo.getSeason();
				episodes.addAll( episodeInfo.getEpisodes() );

				for (ManagedEpisode managedEpisode : existingEpisodes) {
					if (managedEpisode.getSeasonNumber() == seasonNumber && episodes.contains( managedEpisode.getEpisodeNumber() )) {
						
						episodeInfoFound = true;
						
						if (!managedEpisode.isDownloaded()) {
							// cancel search for this episode
							BackLogProcessor.getInstance().unschedule(FindEpisodeTask.class, String.format("task.episode.id == %d", managedEpisode.getId()) );
							DownloadableManager.getInstance().addAllSimilarNamedFiles(p, managedEpisode);
						}
	
						if ( episodeInfo != null ) {
							managedEpisode.setQuality( episodeInfo.getQuality() );
							managedEpisode.setSource( episodeInfo.getSource() );
							managedEpisode.setReleaseGroup( Release.firstMatch( episodeInfo.getRelease() ).name() );
						}
	
						managedEpisode.setAbsoluteNumber( managedEpisode.getEpisodeNumber() );
	
						if ( series.getSubtitlesLanguage() != null ) {
							if ( VideoManager.isAlreadySubtitled( managedEpisode, series.getSubtitlesLanguage() )) {
								BackLogProcessor.getInstance().unschedule( FindSubtitleEpisodeTask.class, String.format("task.episode.id == %d", managedEpisode.getId()) );
							} else {
								BackLogProcessor.getInstance().schedule( new FindSubtitleEpisodeTask( managedEpisode ), false );
							}
						}
	
						downloadableDAO.updateLabel( managedEpisode.getId(), p.getFileName().toString() );
	
						TVShowManager.getInstance().saveEpisode( managedEpisode );
						VideoManager.getInstance().getMetaData(managedEpisode, p);
					}
				}
			}
			
			if (!episodeInfoFound) {
				unrecognizedDAO.createUnrecognizedFile(p, series.getId() );
			}
		}
	}

	@Override
	public Filter<Path> getFileFilter() {
		return null;
	}
	
	private ManagedSeries series = null;
	private List<TVShowSeason> seasons;
	private List<ManagedEpisode> managedEpisodes;
	
	@Override
	public void init() throws Exception {
		super.init();

		series = ((ScanTVShowTask)task).getSeries();

		// cleanup
		unrecognizedDAO.deleteUnrecognizedFiles( series.getId() );

		managedEpisodes = managedEpisodeDAO.findEpisodesForTVShow( series.getId() );
		seasons = tvShowSeasonDAO.findSeasons( series.getId() );
		
		for ( ManagedEpisode episode : managedEpisodes ) {
			boolean hasOneFileLeft = false;
			List<DownloadableFile> episodeFiles = downloadableDAO.getAllFiles( episode.getId() );
			for (DownloadableFile downloadableFile : episodeFiles) {
				if (!Files.exists( downloadableFile.getFilePath() )) {
					downloadableDAO.deleteFile( downloadableFile.getFilePath()	 );
				} else {
					hasOneFileLeft = true;
				}
			}
			
			if (!hasOneFileLeft) {
				episode.setIgnored();
				BackLogProcessor.getInstance().post( new DeleteDownloadableEvent( episode ) );
			}
		}
	}
	
	@Override
	public void parsePath(Path folder) throws Exception {
		parseFolder( series, seasons, managedEpisodes, task.getFolder() );
	}

}
