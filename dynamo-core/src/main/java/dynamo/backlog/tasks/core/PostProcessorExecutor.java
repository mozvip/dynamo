package dynamo.backlog.tasks.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.MoveFileTask;
import dynamo.backlog.tasks.movies.ImportMovieFileTask;
import dynamo.core.FolderIdentifier;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.jdbi.TVShowDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.FolderManager;
import dynamo.manager.games.GamesManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.ISOType;
import dynamo.model.backlog.core.PostProcessFolderTask;
import dynamo.model.games.VideoGame;
import dynamo.model.games.VideoGameDAO;
import dynamo.model.movies.MovieManager;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultFile;
import dynamo.model.tvshows.TVShowManager;
import dynamo.parsers.ParsedMovieInfo;
import dynamo.parsers.TVShowEpisodeInfo;
import dynamo.parsers.VideoInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.utils.ISOFileIdentifier;
import model.ManagedEpisode;
import model.ManagedSeries;

public class PostProcessorExecutor extends TaskExecutor<PostProcessFolderTask> implements Reconfigurable {
	
	@Configurable(category="Post Processor", name="Post process all files (not only the ones downloaded by Dynamo)")
	private boolean allowPostProcessorToManageAllFiles = true;

	@Configurable(category="Post Processor", name="Remove empty folders", defaultValue="true")
	private boolean removeEmptyFolders = true;

	public boolean isAllowPostProcessorToManageAllFiles() {
		return allowPostProcessorToManageAllFiles;
	}

	public void setAllowPostProcessorToManageAllFiles(
			boolean allowPostProcessorToManageAllFiles) {
		this.allowPostProcessorToManageAllFiles = allowPostProcessorToManageAllFiles;
	}

	public boolean isRemoveEmptyFolders() {
		return removeEmptyFolders;
	}

	public void setRemoveEmptyFolders(boolean removeEmptyFolders) {
		this.removeEmptyFolders = removeEmptyFolders;
	}

	private Map<String, Long> fileMap;
	
	private SearchResultDAO searchResultDAO;
	private VideoGameDAO videoGameDAO;
	private TVShowDAO tvShowDAO;
	private DownloadableDAO downloadableDAO;

	public PostProcessorExecutor(PostProcessFolderTask item, SearchResultDAO searchResultDAO, VideoGameDAO videoGameDAO, TVShowDAO tvShowDAO, DownloadableDAO downloadableDAO) {
		super(item);
		this.searchResultDAO = searchResultDAO;
		this.videoGameDAO = videoGameDAO;
		this.tvShowDAO = tvShowDAO;
		this.downloadableDAO = downloadableDAO;
	}

	@Override
	public void execute() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Path pathFolder = task.getFolder();

		if (!Files.isDirectory( pathFolder )) {
			return;
		}

		List<SearchResultFile> results = searchResultDAO.getSearchResultFiles();

		fileMap = new HashMap<String, Long>();
		for (SearchResultFile result : results) {
			fileMap.put( result.getName(), result.getSize() );
		}

		List<Path> files = FolderManager.getAllFilesFrom( pathFolder, removeEmptyFolders );
		List<Path> unknownFiles = new ArrayList<Path>();

		Map<SearchResult, List<Path>> filesBySearchResult = new HashMap<>();

		for (Path path : files) {

			boolean knownFile = false;

			for (SearchResultFile resultFile : results) {

				if (!path.toAbsolutePath().toString().endsWith( resultFile.getName() )) {
					continue;
				}

				if (Files.isDirectory(path)) {
					continue;
				}

				String[] elements = resultFile.getName().split( "/" );
				String pathFileName = path.getFileName().toString();
				Path parent = path.getParent();
				for (int i=1; i<elements.length; i++) {
					pathFileName = parent.getFileName().toString() + "/" + pathFileName;
					parent = parent.getParent();
					if (parent == null) {
						break;
					}
				}

				if (resultFile.getName().equals( pathFileName )) {	// && Files.size(path) == resultFile.getSize()
					SearchResult result = resultFile.getResult();
					if (!filesBySearchResult.containsKey( result )) {
						filesBySearchResult.put( result, new ArrayList<Path>());
					}
					filesBySearchResult.get( result ).add( path );						
					knownFile = true;
				}
			}

			if (!knownFile) {
				unknownFiles.add( path );
			}
		}
		
		for (Entry<SearchResult, List<Path>> entry : filesBySearchResult.entrySet()) {
			
			SearchResult searchResult = entry.getKey();
			
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance( searchResult.getDownloadableId() );
			DownloadableManager.getInstance().downloaded(task, downloadable, searchResult, pathFolder, entry.getValue(), true);
		}

		if (allowPostProcessorToManageAllFiles) {
			postProcessUnknownFiles( unknownFiles );
		}

	}
	
	private void postProcessUnknownFiles( List<Path> unknownFiles) {
		
		Set<Path> identifiedFolders = new HashSet<Path>();
		Set<Path> visitedFolders = new HashSet<Path>();
		
		Set<FolderIdentifier> folderIdentifiers = FolderIdentifierManager.getInstance().getFolderIdentifiers();
		if (folderIdentifiers != null && folderIdentifiers.size() > 0) {
			for (Path path: unknownFiles ) {
				Path folder = path.getParent();
				if (visitedFolders.contains(folder)) {
					continue;
				}
				
				visitedFolders.add( folder );

				boolean alreadyIdentified = false;
				for (Path identifiedFolder : identifiedFolders) {
					if (path.startsWith( identifiedFolder)) {
						alreadyIdentified = true;
						break;
					}
				}
				
				if (alreadyIdentified) {
					continue;
				}
	
				try {
					for (FolderIdentifier folderIdentifier : folderIdentifiers) {
						if (folderIdentifier.is( folder )) {
							folderIdentifier.onIdentify( folder );
							identifiedFolders.add( folder );
							break;
						}
					}
				} catch (Exception e) {
					ErrorManager.getInstance().reportThrowable(e);
				}
			}
		}
		
		for (Path path: unknownFiles ) {

			for (Path identifiedFolder : identifiedFolders) {
				if (path.startsWith( identifiedFolder)) {
					continue;
				}
			}

			if (path.toString().endsWith(".iso")) {
			
				ISOType isoType = ISOFileIdentifier.identify( path );
				if ( isoType.getGamePlatform() != null ) {
					Path destinationFolder = GamesManager.getInstance().getFolder( isoType.getGamePlatform() );
					if (destinationFolder != null ) {
						String gameName = path.getFileName().toString();

						long videoGameId = downloadableDAO.createDownloadable( VideoGame.class, gameName, DownloadableStatus.DOWNLOADED );
						videoGameDAO.save( videoGameId, isoType.getGamePlatform(), null );
						VideoGame videoGame = new VideoGame( videoGameId, DownloadableStatus.DOWNLOADED, gameName, isoType.getGamePlatform(), null );

						Path destination = destinationFolder.resolve( path.getFileName() );
						queue( new MoveFileTask(path, destination, videoGame), false );
					}
				}
			}
			
			if (VideoFileFilter.getInstance().accept( path )) {
				
				VideoInfo info = VideoNameParser.getVideoInfo(path);
				
				if ( MovieManager.getInstance().isEnabled() && info instanceof ParsedMovieInfo ) {

					// movie found
					ParsedMovieInfo movieInfo = (ParsedMovieInfo) info;
					MovieInfo movieDb;
					try {
						movieDb = MovieManager.getInstance().getMovieDb(movieInfo.getName(), movieInfo.getYear());
						if (movieDb != null) {
							BackLogProcessor.getInstance().schedule( new ImportMovieFileTask( path, movieDb ) );
						}
					} catch (MovieDbException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}

				} else if (TVShowManager.getInstance().isEnabled() && info instanceof TVShowEpisodeInfo ) {
					
					TVShowEpisodeInfo episodeInfo = (TVShowEpisodeInfo) info;
					
					ManagedSeries series = TVShowManager.getInstance().findManagedSeries( episodeInfo.getName() );
					if ( series == null ) {
						LOGGER.info( String.format("TVShow %s is not managed by Dynamo, ignoring file %s", episodeInfo.getName(), path.toString()));
					} else {
						List<ManagedEpisode> episodes = tvShowDAO.findEpisodesForTVShowAndSeason( series.getId(), episodeInfo.getSeason() );
						List<ManagedEpisode> associatedEpisodes = new ArrayList<ManagedEpisode>();
						for (ManagedEpisode managedEpisode : episodes) {
							if ( managedEpisode.getEpisodeNumber() >= episodeInfo.getFirstEpisode() &&  managedEpisode.getEpisodeNumber() <= episodeInfo.getLastEpisode() ) {
								associatedEpisodes.add( managedEpisode);
							}
						}

						if (!associatedEpisodes.isEmpty()) {
							
							for (ManagedEpisode managedEpisode : associatedEpisodes) {
								if (managedEpisode.isDownloaded()) {
									// TODO: if episode already existing, only get if new one is better quality ??
									continue;
								}
							}
							Path destinationFolder = series.getFolder().resolve( String.format( TVShowManager.getInstance().getSeasonFolderPattern(), episodeInfo.getSeason()) );
							Path destinationFile = destinationFolder.resolve( path.getFileName() );

							FolderManager.moveFile( path, destinationFile, associatedEpisodes.get(0) );	// FIXME: pass a list of downloadable in case we have several ??
							try {
								FolderManager.moveAssociatedFiles( path, destinationFolder, associatedEpisodes.get(0) );
							} catch (IOException e) {
								ErrorManager.getInstance().reportThrowable( e );
							}

							for (ManagedEpisode episode : associatedEpisodes) {
								DownloadableManager.getInstance().snatched( episode, null );
							}
							
						}
					}
				}
			}
		}
	}
	
	@Override
	public void reconfigure() {
	}

	@Override
	public void rescheduleTask(PostProcessFolderTask item) {
		item.setMinDate( getNextDate( 60 ));	// FIXME: make this a parameter
		queue( item, false );
	}

}
