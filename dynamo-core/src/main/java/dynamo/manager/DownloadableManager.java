package dynamo.manager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.AudioFileFilter;
import dynamo.backlog.tasks.core.CancelDownloadTask;
import dynamo.backlog.tasks.core.SubtitlesFileFilter;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.backlog.tasks.music.ImportMusicFileTask;
import dynamo.core.DownloadFinder;
import dynamo.core.DynamoApplication;
import dynamo.core.DynamoServer;
import dynamo.core.EventManager;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.HistoryDAO;
import dynamo.core.model.Task;
import dynamo.jdbi.MovieDAO;
import dynamo.jdbi.SearchResultDAO;
import dynamo.jdbi.TVShowDAO;
import dynamo.model.DownloadInfo;
import dynamo.model.DownloadLocation;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.SuggestionURLDAO;
import dynamo.model.Video;
import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.ebooks.EBook;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;
import dynamo.model.music.MusicAlbum;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import dynamo.parsers.TVShowEpisodeInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.webapps.pushbullet.PushBullet;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.backlog.ScanTVShowTask;


public class DownloadableManager {
	
	private DynamoObjectFactory<FindDownloadableTask> findDownloadableFactory;
	
	private TVShowDAO tvShowDAO = DAOManager.getInstance().getDAO( TVShowDAO.class );
	private MovieDAO movieDAO = DAOManager.getInstance().getDAO( MovieDAO.class );
	private HistoryDAO historyDAO = DAOManager.getInstance().getDAO( HistoryDAO.class );
	private SearchResultDAO searchResultDAO = DAOManager.getInstance().getDAO( SearchResultDAO.class );
	private SuggestionURLDAO suggestionURLDAO = DAOManager.getInstance().getDAO( SuggestionURLDAO.class );
	
	@Configurable(category="Notifiers", name="Notify on Snatch events", defaultValue="false")
	private boolean notifyOnSnatch;
	
	@Configurable(category="Notifiers", name="Notify on Download events", defaultValue="true")
	private boolean notifyOnDownload;

	public boolean isNotifyOnSnatch() {
		return notifyOnSnatch;
	}

	public void setNotifyOnSnatch(boolean notifyOnSnatch) {
		this.notifyOnSnatch = notifyOnSnatch;
	}

	public boolean isNotifyOnDownload() {
		return notifyOnDownload;
	}

	public void setNotifyOnDownload(boolean notifyOnDownload) {
		this.notifyOnDownload = notifyOnDownload;
	}

	static class SingletonHolder {
		static DownloadableManager instance = new DownloadableManager();
	}
	
	public static DownloadableManager getInstance() {
		return SingletonHolder.instance;
	}
	
	private DownloadableManager() {
		findDownloadableFactory = new DynamoObjectFactory<FindDownloadableTask>( "dynamo", FindDownloadableTask.class);
	}
	
	private DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );

	public void want( Downloadable downloadable ) {
		downloadable.setWanted();
		downloadableDAO.updateStatus(downloadable.getId(), DownloadableStatus.WANTED);
		scheduleFind( downloadable );
	}
	
	public void want(long downloadableId) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		Downloadable downloadable = DownloadableFactory.getInstance().createInstance(downloadableId);
		want( downloadable );
	}	
	
	public void scheduleFind( Downloadable downloadable ) {
		BackLogProcessor.getInstance().schedule( findDownloadableFactory.newInstance( downloadable ), false );
	}
	
	public int updateStatus( Downloadable downloadable, DownloadableStatus newStatus ) {
		return downloadableDAO.updateStatus(downloadable.getId(), newStatus);
	}

	public long createDownloadable(Class<?> klass, String name, String coverImage, DownloadableStatus status) {
		return downloadableDAO.createDownloadable( klass, name, coverImage, status );
	}
	
	public long createSuggestion(Class<?> klass, String name, String coverImage, String suggestionURL) {
		long downloadableId = downloadableDAO.createDownloadable( klass, name, coverImage, DownloadableStatus.SUGGESTED );
		suggestionURLDAO.saveSuggestionURL(downloadableId, suggestionURL);
		return downloadableId;
	}

	public void logStatusChange( Downloadable downloadable, DownloadableStatus newStatus) {
		logStatusChange(downloadable, newStatus, null);
	}
	
	public void logStatusChange( Downloadable downloadable, DownloadableStatus newStatus, String comment ) {
		if (updateStatus( downloadable, newStatus ) == 1) {
			if (downloadable != null) {
				if (comment != null) {
					historyDAO.insert( comment, newStatus, downloadable.getId() );
					EventManager.getInstance().reportSuccess( comment );
				}
				String absoluteURL = String.format("http://%s:%d/%s", DynamoApplication.getInstance().getIpAddress(),  DynamoServer.getInstance().getPort(), downloadable.getRelativeLink() );
				if (notifyOnDownload && newStatus == DownloadableStatus.DOWNLOADED && PushBullet.getInstance().isEnabled()) {
					PushBullet.getInstance().pushLink( "Dynamo has downloaded something", downloadable.toString(), absoluteURL );
				}
				if (notifyOnSnatch && newStatus == DownloadableStatus.SNATCHED && PushBullet.getInstance().isEnabled()) {
					PushBullet.getInstance().pushLink( "Dynamo has snatched something", downloadable.toString(), absoluteURL );
				}
			}
		}
	}
	
	public void snatched( Downloadable downloadable, SearchResult result ) {
		
		if (result != null) {
		
			DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.SNATCHED,
					String.format("<a href='%s'>%s</a> has been snatched from %s : %s", downloadable.getRelativeLink(), downloadable.toString(), result.getProviderName(), result.getTitle()) );
			downloadableDAO.updateLabel(downloadable.getId(), result.getTitle());
			
		} else {
			
			DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.SNATCHED,
					String.format("<a href='%s'>%s</a> has been snatched", downloadable.getRelativeLink(), downloadable.toString()) );
			
		}
	}

	public void newFile( Task task, Downloadable downloadable, Path newFile ) {
		
		if (Files.isDirectory( newFile )) {
			return;
		}
		
		Long id = downloadable.getId();

		addFile(id, newFile, 0);
		
		if ( downloadable instanceof MusicAlbum) {
			
			MusicAlbum musicAlbum = ( MusicAlbum ) downloadable;
			try {
				if (AudioFileFilter.getInstance().accept(newFile)) {
					BackLogProcessor.getInstance().schedule( new ImportMusicFileTask( musicAlbum, newFile, false ), false );
				}
			} catch (IOException e) {
			}
		} else {

			if (downloadable instanceof Video) {
				
				String fileName = newFile.getFileName().toString();
				
				if ( VideoFileFilter.getInstance().accept( newFile ) ) {
					try {
						if (fileName.contains("-sample") || fileName.startsWith("sample-") || Files.size(newFile) < (50*1024*1024)) {
							BackLogProcessor.getInstance().schedule( new DeleteTask( newFile, false ));	// FIXME : should have been done earlier, by the post processor ?
						} else {
							if (downloadable instanceof ManagedEpisode) {
								ManagedSeries series = TVShowManager.getInstance().getManagedSeries(((ManagedEpisode) downloadable).getSeriesId());
								if ( TVShowManager.getInstance().isAlreadySubtitled( downloadable, series.getSubtitleLanguage() )) {
									tvShowDAO.setSubtitled( id, ((ManagedEpisode) downloadable).getSubtitlesPath() );
								}
							} else if (downloadable instanceof Movie) {
								if ( TVShowManager.getInstance().isAlreadySubtitled( downloadable, MovieManager.getInstance().getSubtitlesLanguage() )) {
									movieDAO.setSubtitled( id, ((Movie) downloadable).getSubtitlesPath() );
								}
							}
						}
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable(task, e);
					}
					
				} else if (SubtitlesFileFilter.getInstance().accept( newFile )) {
					((Video)downloadable).setSubtitlesPath( newFile );								
					((Video)downloadable).setSubtitled( true );								
				}

			} else if (downloadable instanceof TVShowSeason) {

				if ( VideoFileFilter.getInstance().accept( newFile ) ) {
					TVShowSeason season = TVShowManager.getInstance().findSeason( id );
					ManagedSeries series = TVShowManager.getInstance().getManagedSeries( season.getSeriesId() );

					TVShowEpisodeInfo episodeInfo = VideoNameParser.getTVShowEpisodeInfo(series, newFile);
					if (episodeInfo != null) {
						// TODO
					}

					BackLogProcessor.getInstance().schedule( new ScanTVShowTask( series ));
				}

			}

		}
	}

	public List<DownloadInfo> findWanted() {
		return downloadableDAO.findWanted();
	}

	public void downloaded(Task task, Downloadable downloadable, SearchResult searchResult, Path sourceFolder, List<Path> sourceFiles, boolean moveFiles) throws IOException {
		
		boolean filesFound = true;
		
		Path destinationFolder = downloadable.determineDestinationFolder();
		for (Path source : sourceFiles) {
			
			if (Files.isDirectory(source)) {
				continue;
			}

			if (!Files.isReadable( source )) {
				// report error
				ErrorManager.getInstance().reportError(task, String.format("Cannot read %s", source.toAbsolutePath().toString()));
				filesFound = false;
				break;
			}

			Path destinationFile = null;
			if ( downloadable instanceof MusicAlbum || downloadable instanceof ManagedEpisode ) {
				// we want to use our clean folder name, and remove any intermediate folder that may have been added in the download package
				destinationFile = destinationFolder.resolve( source.getFileName() );
			} else {
				destinationFile = destinationFolder.resolve( source.subpath(sourceFolder.getNameCount(), source.getNameCount()) );
			}

			if (moveFiles) {
				FolderManager.moveFile(source, destinationFile, downloadable);
			} else {
				FolderManager.copyFile(source, destinationFile, downloadable);
			}
		}
		
		if (filesFound) {
			logStatusChange( downloadable, DownloadableStatus.DOWNLOADED, String.format("<a href='%s'>%s</a> has been downloaded", downloadable.getRelativeLink(), downloadable.toString()) );
		}

	}

	public void delete(Class<? extends Downloadable> klass, DownloadableStatus statusToDelete) {
		downloadableDAO.delete(klass, statusToDelete);
	}

	public void delete(long id) {
		downloadableDAO.delete(id);
	}

	public void redownload(Downloadable downloadable) {
		// blacklist search result
		searchResultDAO.blacklist(downloadable.getId());
		// delete all corresponding files
		getAllFiles(downloadable.getId()).forEach( downloadedFile -> BackLogProcessor.getInstance().schedule( new DeleteTask(downloadedFile.getFilePath(), true), false ));
		want(downloadable);
	}

	public void redownload( long downloadableId ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		// blacklist search result
		searchResultDAO.blacklist( downloadableId );
		want(downloadableId);
	}
	
	public void blackListSearchResult( String url ) {
		searchResultDAO.blacklist( url );
	}

	public void ignore(Downloadable downloadable) {
		DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.IGNORED, String.format("<a href='%s'>%s</a> is now ignored", downloadable.getRelativeLink(), downloadable.toString()) );
		BackLogProcessor.getInstance().unschedule( String.format( "this.downloadable.id == %d", downloadable.getId() ) );
	}

	public void suggest( Downloadable downloadable, String url ) {
		DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.SUGGESTED );
		suggestionURLDAO.saveSuggestionURL(downloadable.getId(), url );
	}
	
	public void cancelDownload( Downloadable downloadable ) {
		List<SearchResult> searchResults = searchResultDAO.getSearchResults( downloadable.getId() );
		for (SearchResult searchResult : searchResults) {
			if (!searchResult.isBlackListed() && StringUtils.isNotEmpty( searchResult.getClientId() )) {
				BackLogProcessor.getInstance().schedule( new CancelDownloadTask( downloadable, searchResult ));
			}
		}
	}

	public List<DownloadInfo> findDownloaded() {
		return downloadableDAO.findDownloaded();
	}

	public boolean isBlackListed(Path path, Downloadable downloadable) {
		
		String fileName = path.getFileName().toString();
		
		if (fileName.endsWith(".nzb") || fileName.endsWith(".lnk") || fileName.endsWith(".URL")) {
			return true;
		}
		if (fileName.endsWith(".exe") || fileName.endsWith(".scr")) {
			if (downloadable instanceof MusicAlbum || downloadable instanceof Video || downloadable instanceof EBook) {
				return true;
			}
		}
		return false;
	}

	public void saveResult(String title, String url, String providerName, Class<? extends DownloadFinder> providerClass, String referer, float sizeInMegs,
			SearchResultType downloadType, long downloadableId) {
		
		searchResultDAO.save(url, providerName, providerClass, referer, sizeInMegs, title, downloadType, downloadableId, null);
		
	}

	public void updateCoverImage(long id, String image) {
		downloadableDAO.updateCoverImage( id, image );
	}

	public Set<DownloadableFile> getAllFiles( Class<? extends Downloadable> downloadableClass) {
		return downloadableDAO.getAllFiles( downloadableClass );
	}

	public void addFile( long downloadableId, Path file, long size, int index ) throws IOException {
		downloadableDAO.addFile( downloadableId, file, size, index );
	}

	public void addFile( long downloadableId, Path file, int index ) {
		try {
			downloadableDAO.addFile( downloadableId, file, Files.size( file ), index );
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}

	public Stream<DownloadableFile> getAllFiles(long downloadableId) {
		List<DownloadableFile> files = downloadableDAO.getAllFiles( downloadableId );
		return files.stream();
	}

	public void clearBlackList() {
		searchResultDAO.clearBlackList();
	}
	
	public void saveDownloadLocations(long downloadableId, String title, String suggesterName,  Class<? extends DownloadFinder> providerClass, String referer, float size, Collection<DownloadLocation> downloadLocations) {
		if (downloadableId >= 0 && downloadLocations != null && !downloadLocations.isEmpty()) {
			for (DownloadLocation downloadLocation : downloadLocations) {
				saveResult(title, downloadLocation.getUrl(), suggesterName, providerClass, referer, size, downloadLocation.getType(), downloadableId);
			}
		}		
	}
	
	public void setAkas( long downloadableId, Collection<String> akas ) {
		downloadableDAO.saveAka(downloadableId, akas);
	}

	public DownloadInfo find(long downloadableId) {
		return downloadableDAO.find(downloadableId);
	}

	public void ignore(long downloadableId) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		Downloadable downloadable = DownloadableFactory.getInstance().createInstance(downloadableId);
		ignore( downloadable );	
	}

	public DownloadableFile getFile(Path p) {
		return downloadableDAO.getFile( p );
	}

	public void saveSuggestionURL(long downloadableId, String suggestionURL) {
		suggestionURLDAO.saveSuggestionURL(downloadableId, suggestionURL);
	}

}
