package dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.ScanFolderExecutor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.ReleaseGroup;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.FolderManager;
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
						DownloadableManager.getInstance().addAllSimilarNamedFiles(p, managedEpisode);
					}

					if ( episodeInfo != null ) {
						managedEpisode.setQuality( episodeInfo.getQuality() );
						managedEpisode.setSource( episodeInfo.getSource() );
						managedEpisode.setReleaseGroup( ReleaseGroup.firstMatch( episodeInfo.getRelease() ).name() );
					}

					managedEpisode.setAbsoluteNumber( managedEpisode.getEpisodeNumber() );

					if ( series.getSubtitlesLanguage() != null ) {
						if ( VideoManager.isAlreadySubtitled( managedEpisode, series.getSubtitlesLanguage() )) {
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
	}
	
	@Override
	public void parsePath(Path folder) throws Exception {
		parseFolder( series, seasons, managedEpisodes, task.getFolder() );
	}

}
