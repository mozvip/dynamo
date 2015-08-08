package dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.ReleaseGroup;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.TVShowDAO;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.find.FindEpisodeTask;
import dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import dynamo.model.tvshows.TVShowManager;
import dynamo.parsers.TVShowEpisodeInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.video.VideoManager;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.backlog.ScanTVShowTask;

public class ScanTVShowExecutor extends TaskExecutor<ScanTVShowTask> {
	
	private TVShowDAO tvShowDAO;
	private DownloadableDAO downloadableDAO;

	public ScanTVShowExecutor(ScanTVShowTask item, TVShowDAO tvShowDAO, DownloadableDAO downloadableDAO) {
		super(item);
		this.tvShowDAO = tvShowDAO;
		this.downloadableDAO = downloadableDAO;
	}

	private void parseFolder( ManagedSeries series, List<ManagedEpisode> existingEpisodes, Path folder ) throws IOException, InterruptedException {

		if (! Files.isReadable( folder )) {
			return;
		}

		DirectoryStream<Path> ds = Files.newDirectoryStream( folder, VideoFileFilter.getInstance() );

		for (Path p : ds) {
			if (Files.isDirectory(p)) {
				parseFolder( series, existingEpisodes, p );
			} else {

				Integer seasonNumber = null;
				List<Integer> episodes = new ArrayList<Integer>();
				
				TVShowEpisodeInfo episodeInfo = VideoNameParser.getTVShowEpisodeInfo(series, p);
				if ( episodeInfo == null ) {
					
					for (ManagedEpisode episode : existingEpisodes) {
						if (p.equals( episode.getPath())) {
							seasonNumber = episode.getSeasonNumber();
							episodes.add( episode.getEpisodeNumber() );
						}
					}
					if (episodes.size() == 0) {
						tvShowDAO.createUnrecognizedFile(p, task.getSeries().getId() );
						continue;
					}

				} else {
					
					seasonNumber = episodeInfo.getSeason();
					episodes.addAll( episodeInfo.getEpisodes() );

				}
				
				for (ManagedEpisode managedEpisode : existingEpisodes) {
					if (managedEpisode.getSeasonNumber() == seasonNumber && episodes.contains( managedEpisode.getEpisodeNumber() )) {
						
						if (!managedEpisode.isDownloaded()) {
							// cancel search for this episode
							BackLogProcessor.getInstance().unschedule(FindEpisodeTask.class, String.format("this.episode.id == %d", managedEpisode.getId()) );
						}

						downloadableDAO.updatePathAndStatus( managedEpisode.getId(), p, DownloadableStatus.DOWNLOADED );

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

						TVShowManager.getInstance().saveEpisode( managedEpisode );
						
						VideoManager.getInstance().getMetaData(managedEpisode, p);

					}
				}
			}
		}
	}

	@Override
	public void execute() throws IOException, InterruptedException {
		
		// cleanup
		tvShowDAO.deleteUnrecognizedFiles( task.getSeries().getId() );

		List<ManagedEpisode> managedEpisodes = tvShowDAO.findEpisodesForTVShow( task.getSeries().getId() );
		for ( ManagedEpisode managedEpisode : managedEpisodes ) {
			if (managedEpisode.isDownloaded()) {
				Path p = managedEpisode.getPath();
				if (!Files.exists( p ) || Files.isDirectory( p )) {
					managedEpisode.setReleaseGroup( null );
					managedEpisode.setSource( null );
					managedEpisode.setQuality( null );
					managedEpisode.setSubtitled( false );

					downloadableDAO.nullifyPath( managedEpisode.getId() );
					TVShowManager.getInstance().saveEpisode( managedEpisode );
				}
			}
		}

		parseFolder( task.getSeries(), managedEpisodes, task.getSeries().getFolder() );
	}

}
