package dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.ReleaseGroup;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.backlog.find.FindEpisodeTask;
import dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import dynamo.parsers.TVShowEpisodeInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import dynamo.tvshows.jdbi.TVShowSeasonDAO;
import dynamo.tvshows.jdbi.UnrecognizedDAO;
import dynamo.tvshows.model.ManagedEpisode;
import dynamo.tvshows.model.ManagedSeries;
import dynamo.tvshows.model.TVShowManager;
import dynamo.tvshows.model.TVShowSeason;
import dynamo.video.VideoManager;
import model.backlog.ScanTVShowTask;

public class ScanTVShowExecutor extends TaskExecutor<ScanTVShowTask> {
	
	private TVShowSeasonDAO tvShowSeasonDAO;
	private DownloadableUtilsDAO downloadableDAO;
	private ManagedEpisodeDAO managedEpisodeDAO;
	private UnrecognizedDAO unrecognizedDAO;

	public ScanTVShowExecutor(ScanTVShowTask item, TVShowSeasonDAO tvShowSeasonDAO, DownloadableUtilsDAO downloadableDAO, ManagedEpisodeDAO managedEpisodeDAO, UnrecognizedDAO unrecognizedDAO) {
		super(item);
		this.tvShowSeasonDAO = tvShowSeasonDAO;
		this.downloadableDAO = downloadableDAO;
		this.managedEpisodeDAO = managedEpisodeDAO;
		this.unrecognizedDAO = unrecognizedDAO;
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
				
				TVShowEpisodeInfo episodeInfo = VideoNameParser.getTVShowEpisodeInfo(series, p);
				
				boolean episodeInfoFound = false;
				
				if ( episodeInfo == null ) {
					DownloadableFile downloadableFile = DownloadableManager.getInstance().getFile( p );
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

							DownloadableManager.getInstance().addAssociatedFiles(p, managedEpisode);
							DownloadableManager.getInstance().addFile( managedEpisode, p );
						}

						if ( episodeInfo != null ) {
							managedEpisode.setQuality( episodeInfo.getQuality() );
							managedEpisode.setSource( episodeInfo.getSource() );
							managedEpisode.setReleaseGroup( ReleaseGroup.firstMatch( episodeInfo.getRelease() ).name() );
						}

						managedEpisode.setAbsoluteNumber( managedEpisode.getEpisodeNumber() );

						if ( task.getSeries().getSubtitlesLanguage() != null ) {
							if ( VideoManager.isAlreadySubtitled( managedEpisode, task.getSeries().getSubtitlesLanguage() )) {
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
					unrecognizedDAO.createUnrecognizedFile(p, task.getSeries().getId() );
				}
			}
		}
	}



	@Override
	public void execute() throws IOException, InterruptedException {

		// cleanup
		unrecognizedDAO.deleteUnrecognizedFiles( task.getSeries().getId() );

		List<ManagedEpisode> managedEpisodes = managedEpisodeDAO.findEpisodesForTVShow( task.getSeries().getId() );
		List<TVShowSeason> seasons = tvShowSeasonDAO.findSeasons( task.getSeries().getId() );
		
		for ( ManagedEpisode managedEpisode : managedEpisodes ) {
			if (managedEpisode.isDownloaded()) {
				Optional<Path> mainVideoFile = VideoManager.getInstance().getMainVideoFile( managedEpisode.getId() );
				if (mainVideoFile.isPresent()) {
					Path path = mainVideoFile.get();
					if (!Files.exists( path )) {
						BackLogProcessor.getInstance().schedule( new DeleteFileTask( path ), false);
					}
				} else {
					TVShowManager.getInstance().ignoreOrDeleteEpisode( managedEpisode );
				}
			}
		}

		parseFolder( task.getSeries(), seasons, managedEpisodes, task.getSeries().getFolder() );
	}

}
