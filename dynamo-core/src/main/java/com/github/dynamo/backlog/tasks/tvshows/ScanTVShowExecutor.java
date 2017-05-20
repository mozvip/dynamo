package com.github.dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.ScanFolderExecutor;
import com.github.dynamo.backlog.tasks.core.SubtitlesFileFilter;
import com.github.dynamo.backlog.tasks.core.VideoFileFilter;
import com.github.dynamo.backlog.tasks.files.DeleteDownloadableEvent;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.FolderManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.backlog.find.FindEpisodeTask;
import com.github.dynamo.parsers.TVShowEpisodeInfo;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.subtitles.FindSubtitleEpisodeTask;
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
	
	public List<Path> getAssociatedFiles( Path file, List<Path> fromList ) {
		List<Path> matches = new ArrayList<>();
		String fileName = file.getFileName().toString();
		final String filePrefix = fileName.substring(0,  fileName.lastIndexOf('.'));
		for (Path path : fromList) {
			String pathName = path.getFileName().toString();
			if (pathName.startsWith( filePrefix )) {
				matches.add( path );
			}
		}
		return matches;
	}

	private void parseFolder( ManagedSeries series, List<TVShowSeason> seasons, List<ManagedEpisode> existingEpisodes, Path folder ) throws IOException, InterruptedException {
		
		for (TVShowSeason season : seasons) {
			// No files should be associated to seasons
			downloadableDAO.deleteFiles( season.getId() );
		}
		
		List<Path> allFiles = FolderManager.getInstance().getAllFilesFrom(folder, true);
		for (Path p : allFiles) {

			if (!VideoFileFilter.getInstance().accept( p )) {
				continue;
			}
			
			ManagedEpisode currentEpisode = null;

			DownloadableFile downloadableFile = DownloadableManager.getInstance().getFile( p );
			if (downloadableFile != null) {
				currentEpisode = (ManagedEpisode) DownloadableFactory.getInstance().createInstance( downloadableFile.getDownloadableId() );
			}

			TVShowEpisodeInfo episodeInfo = VideoNameParser.getTVShowEpisodeInfo(series, p);
			
			if ( episodeInfo != null ) {

				Collection<Integer> episodes = episodeInfo.getEpisodes();
				if (currentEpisode == null) {
					Optional<ManagedEpisode> ep = existingEpisodes.stream().filter( managedEpisode -> managedEpisode.getSeasonNumber() == episodeInfo.getSeason() && episodes.contains( managedEpisode.getEpisodeNumber() ) ).findFirst();
					if (ep.isPresent()) {
						currentEpisode = ep.get();
					}
				}

				currentEpisode.setQuality( episodeInfo.getQuality() );
				currentEpisode.setSource( episodeInfo.getSource() );
				currentEpisode.setReleaseGroup( Release.firstMatch( episodeInfo.getRelease() ).name() );

			}
			
			if (currentEpisode == null) {
				unrecognizedDAO.createUnrecognizedFile(p, series.getId() );
				continue;
			}
			
			boolean subtitled = false;

			downloadableDAO.deleteFiles( currentEpisode.getId() );
			List<Path> fileGroup = getAssociatedFiles(p, allFiles);
			for (Path file : fileGroup) {
				DownloadableManager.getInstance().addFile( currentEpisode, file );
				if (SubtitlesFileFilter.getInstance().accept( file )) {
					subtitled = true;
				}
			}

			TVShowManager.getInstance().saveEpisode( currentEpisode );
			VideoManager.getInstance().getMetaData(currentEpisode, p);

			currentEpisode.setAbsoluteNumber( currentEpisode.getEpisodeNumber() );
			
			// subtitles search
			if ( series.getSubtitlesLanguage() != null ) {
				if ( !subtitled && !VideoManager.isAlreadySubtitled( currentEpisode, series.getSubtitlesLanguage() )) {
					BackLogProcessor.getInstance().schedule( new FindSubtitleEpisodeTask( currentEpisode ), false );
				} else {
					BackLogProcessor.getInstance().unschedule( FindSubtitleEpisodeTask.class, String.format("task.episode.id == %d", currentEpisode.getId()) );
				}
			}

			// cancel search for this episode
			BackLogProcessor.getInstance().unschedule(FindEpisodeTask.class, String.format("task.episode.id == %d", currentEpisode.getId()) );
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
			
			if (!hasOneFileLeft && episode.getStatus() == DownloadableStatus.DOWNLOADED) {
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
