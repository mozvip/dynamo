package dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.ReleaseGroup;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.find.FindEpisodeTask;
import dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import dynamo.parsers.TVShowEpisodeInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import dynamo.tvshows.jdbi.TVShowDAO;
import dynamo.tvshows.jdbi.TVShowSeasonDAO;
import dynamo.video.VideoManager;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.backlog.ScanTVShowTask;

public class ScanTVShowExecutor extends TaskExecutor<ScanTVShowTask> {
	
	private TVShowDAO tvShowDAO;
	private TVShowSeasonDAO tvShowSeasonDAO;
	private DownloadableUtilsDAO downloadableDAO;
	private ManagedEpisodeDAO managedEpisodeDAO;

	public ScanTVShowExecutor(ScanTVShowTask item, TVShowDAO tvShowDAO, TVShowSeasonDAO tvShowSeasonDAO, DownloadableUtilsDAO downloadableDAO, ManagedEpisodeDAO managedEpisodeDAO) {
		super(item);
		this.tvShowDAO = tvShowDAO;
		this.tvShowSeasonDAO = tvShowSeasonDAO;
		this.downloadableDAO = downloadableDAO;
		this.managedEpisodeDAO = managedEpisodeDAO;
	}

	private void parseFolder( ManagedSeries series, List<TVShowSeason> seasons, List<ManagedEpisode> existingEpisodes, Path folder ) throws IOException, InterruptedException {

		if (! Files.isReadable( folder )) {
			return;
		}

		DirectoryStream<Path> ds = Files.newDirectoryStream( folder, VideoFileFilter.getInstance() );
		for (Path p : ds) {
			if (Files.isDirectory(p)) {
				parseFolder( series, seasons, existingEpisodes, p );
			} else {

				int seasonNumber = -1;
				List<Integer> episodes = new ArrayList<Integer>();
				
				DownloadableFile downloadableFile = DownloadableManager.getInstance().getFile( p );
				
				TVShowEpisodeInfo episodeInfo = VideoNameParser.getTVShowEpisodeInfo(series, p);
				
				boolean episodeInfoFound = false;
				
				if ( episodeInfo == null ) {
					
					if ( downloadableFile != null) {
						Optional<ManagedEpisode> optEpisode = existingEpisodes.stream()
								.filter( episode -> downloadableFile.getDownloadableId() == episode.getId() )
								.findFirst();
						if (optEpisode.isPresent()) {
							seasonNumber = optEpisode.get().getSeasonNumber();
							episodes.add( optEpisode.get().getEpisodeNumber() );
							
							episodeInfoFound = true;
						}
					}
					
				} else {
					
					seasonNumber = episodeInfo.getSeason();
					episodes.addAll( episodeInfo.getEpisodes() );

				}
				
				for (ManagedEpisode managedEpisode : existingEpisodes) {
					if (managedEpisode.getSeasonNumber() == seasonNumber && episodes.contains( managedEpisode.getEpisodeNumber() )) {
						
						episodeInfoFound = true;
						
						if (!managedEpisode.isDownloaded()) {
							// cancel search for this episode
							BackLogProcessor.getInstance().unschedule(FindEpisodeTask.class, String.format("this.episode.id == %d", managedEpisode.getId()) );
							downloadableDAO.updateStatus( managedEpisode.getId(), DownloadableStatus.DOWNLOADED );
							DownloadableManager.getInstance().addFile( managedEpisode.getId(), p, 0);
						}

						if ( episodeInfo != null ) {
							managedEpisode.setQuality( episodeInfo.getQuality() );
							managedEpisode.setSource( episodeInfo.getSource() );
							managedEpisode.setReleaseGroup( ReleaseGroup.firstMatch( episodeInfo.getRelease() ).name() );
						}

						managedEpisode.setSubtitlesPath( null );
						managedEpisode.setSubtitled( false );
						managedEpisode.setAbsoluteNumber( managedEpisode.getEpisodeNumber() );

						if ( task.getSeries().getSubtitleLanguage() != null ) {
							if ( TVShowManager.getInstance().isAlreadySubtitled( managedEpisode, task.getSeries().getSubtitleLanguage() )) {
								managedEpisode.setSubtitled( true );
								BackLogProcessor.getInstance().unschedule( FindSubtitleEpisodeTask.class, String.format("this.episode.id == %d", managedEpisode.getId()) );
							} else {
								BackLogProcessor.getInstance().schedule( new FindSubtitleEpisodeTask( managedEpisode ), false );
							}
						}

						downloadableDAO.updateLabel( managedEpisode.getId(), p.getFileName().toString() );

						TVShowManager.getInstance().saveEpisode( managedEpisode );
						
						VideoManager.getInstance().getMetaData(managedEpisode, p);
					}
				}
				
				if (!episodeInfoFound) {
					tvShowDAO.createUnrecognizedFile(p, task.getSeries().getId() );
				}
			}
		}
	}

	@Override
	public void execute() throws IOException, InterruptedException {

		// cleanup
		tvShowDAO.deleteUnrecognizedFiles( task.getSeries().getId() );

		List<ManagedEpisode> managedEpisodes = managedEpisodeDAO.findEpisodesForTVShow( task.getSeries().getId() );
		List<TVShowSeason> seasons = tvShowSeasonDAO.findSeasons( task.getSeries().getId() );
		
		for ( ManagedEpisode managedEpisode : managedEpisodes ) {
			if (managedEpisode.isDownloaded()) {
				List<Path> episodePaths = DownloadableManager.getInstance().getAllFiles( managedEpisode.getId() ).map( downloadedFile -> downloadedFile.getFilePath() ).collect( Collectors.toList() );
				boolean videoFileFound = false;
				if (episodePaths != null && !episodePaths.isEmpty()) {
					for (Path path : episodePaths) {
						if (Files.exists( path)) {
							if (VideoFileFilter.getInstance().accept(path) && !path.getFileName().toString().contains("-sample")) {
								videoFileFound = true;
							}
						} else {
							BackLogProcessor.getInstance().schedule( new DeleteFileTask( path ));
						}
					}
				}
				if (!videoFileFound) {
					TVShowManager.getInstance().ignoreOrDeleteEpisode( managedEpisode );
				}
			}
		}

		parseFolder( task.getSeries(), seasons, managedEpisodes, task.getSeries().getFolder() );
	}

}
