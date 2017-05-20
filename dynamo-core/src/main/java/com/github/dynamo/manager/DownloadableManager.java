package com.github.dynamo.manager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.CancelDownloadEvent;
import com.github.dynamo.backlog.tasks.core.VideoFileFilter;
import com.github.dynamo.backlog.tasks.files.DeleteFileEvent;
import com.github.dynamo.backlog.tasks.tvshows.ScanTVShowTask;
import com.github.dynamo.core.DynamoApplication;
import com.github.dynamo.core.DynamoServer;
import com.github.dynamo.core.EventManager;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DownloableCount;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.HistoryDAO;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.ebooks.model.EBook;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.model.DownloadInfo;
import com.github.dynamo.model.DownloadLocation;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.SuggestionURLDAO;
import com.github.dynamo.model.Video;
import com.github.dynamo.model.backlog.core.FindDownloadableTask;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.dynamo.parsers.TVShowEpisodeInfo;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.tvshows.jdbi.UnrecognizedDAO;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.github.dynamo.tvshows.model.TVShowSeason;
import com.github.dynamo.video.VideoManager;
import com.github.dynamo.webapps.pushbullet.PushBullet;
import com.github.mozvip.hclient.HTTPClient;


public class DownloadableManager {
	
	private HistoryDAO historyDAO = DAOManager.getInstance().getDAO( HistoryDAO.class );
	private SearchResultDAO searchResultDAO = DAOManager.getInstance().getDAO( SearchResultDAO.class );
	private SuggestionURLDAO suggestionURLDAO = DAOManager.getInstance().getDAO( SuggestionURLDAO.class );
	private UnrecognizedDAO unrecognizedDAO = DAOManager.getInstance().getDAO( UnrecognizedDAO.class );
	
	@Configurable(defaultValue="false")
	private boolean notifyOnSnatch;
	
	@Configurable(defaultValue="true")
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
	
	private Set<Class<? extends Downloadable>> downloadableTypes;
	
	private DownloadableManager() {
		downloadableTypes = DynamoObjectFactory.getReflections().getSubTypesOf(Downloadable.class);
	}
	
	private DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

	public void want( Downloadable downloadable ) {
		updateStatus(downloadable, DownloadableStatus.WANTED);
		downloadableDAO.updateLabel(downloadable.getId(), "");
		scheduleFind( downloadable );
	}
	
	public void want(long downloadableId) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		Downloadable downloadable = DownloadableFactory.getInstance().createInstance(downloadableId);
		want( downloadable );
	}	
	
	public void scheduleFind( Downloadable downloadable ) {
		BackLogProcessor.getInstance().schedule( DynamoObjectFactory.createInstance( FindDownloadableTask.class, downloadable ), false );
	}
	
	public int updateStatus( Downloadable downloadable, DownloadableStatus newStatus ) {
		downloadable.setStatus( newStatus );
		return downloadableDAO.updateStatus(downloadable.getId(), newStatus);
	}

	public long createDownloadable(Class<?> klass, String name, int year, DownloadableStatus status) {
		return downloadableDAO.createDownloadable( klass, name, status, year );
	}
	
	public long createSuggestion(Class<?> klass, String name, int year, String suggestionURL) {
		long downloadableId = downloadableDAO.createDownloadable( klass, name, DownloadableStatus.SUGGESTED, year );
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
					absoluteURL += "DOWNLOADED";
					try {
						PushBullet.getInstance().pushLink( "'%s' was downloaded", downloadable.toString(), absoluteURL );
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
				if (notifyOnSnatch && newStatus == DownloadableStatus.SNATCHED && PushBullet.getInstance().isEnabled()) {
					absoluteURL += "SNATCHED";
					try {
						PushBullet.getInstance().pushLink( "'%s' was snatched", downloadable.toString(), absoluteURL );
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}
		}
	}
	
	public static List<Path> getAssociatedFiles( Path file, List<Path> fromList ) {
		List<Path> matches = new ArrayList<>();
		String fileName = file.getFileName().toString();
		final String filePrefix = fileName.substring(0,  fileName.lastIndexOf('.'));
		for (Path path : fromList) {
			if (path.getFileName().toString().startsWith( filePrefix )) {
				matches.add( path );
			}
		}
		return matches;
	}	
	
	public void snatched( Downloadable downloadable, SearchResult result ) {
		
		if (result != null) {
			
			searchResultDAO.setDownloaded( result.getUrl() );
			DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.SNATCHED,
					String.format("<a href='%s'>%s</a> has been snatched from <a href='%s' target='_blank'>%s</a> : %s", downloadable.getRelativeLink(), downloadable.toString(), result.getReferer(), result.getProviderName(), result.getTitle()) );
			downloadableDAO.updateLabel(downloadable.getId(), result.getTitle());
			
		} else {
			
			DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.SNATCHED,
					String.format("<a href='%s'>%s</a> has been snatched", downloadable.getRelativeLink(), downloadable.toString()) );
			
		}
	}
	
	public long addFile( Downloadable downloadable, Path newFile ) throws IOException {
		return addFile( downloadable, newFile, 0 );
	}

	public long addFile( Downloadable downloadable, Path newFile, int fileIndex ) throws IOException {
		
		Long id = downloadable.getId();

		long fileId = downloadableDAO.createFile( id, newFile, Files.size( newFile ), fileIndex );
		if (downloadable.getStatus() != DownloadableStatus.DOWNLOADED) {
			updateStatus( downloadable, DownloadableStatus.DOWNLOADED );
		}

		unrecognizedDAO.deleteUnrecognizedFile( newFile );

		if (downloadable instanceof Video && VideoManager.isMainVideoFile(newFile)) {
			
			downloadableDAO.updateLabel( id, newFile.getFileName().toString() );

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
		
		return fileId;
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
		
		if (searchResult.getClientId() != null) {
			searchResultDAO.freeClientId(searchResult.getClientId());
		}
	}

	public void delete(Class<? extends Downloadable> klass, DownloadableStatus statusToDelete) {
		downloadableDAO.delete(klass, statusToDelete);
	}

	public void delete(long downloadableId) {
		downloadableDAO.delete(downloadableId);
		searchResultDAO.deleteResultForDownloadableId(downloadableId);
	}

	public void redownload(Downloadable downloadable) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		redownload(downloadable.getId(), false);
	}

	public void redownload( long downloadableId, boolean resetExistingSearchs ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		cancelDownload(downloadableId);
		if (resetExistingSearchs) {
			searchResultDAO.deleteResultForDownloadableId(downloadableId);
		} else {
			// blacklist downloaded search result
			searchResultDAO.blacklistDownloaded( downloadableId );
		}
		// delete all corresponding files
		getAllFiles(downloadableId).forEach(
				downloadedFile -> BackLogProcessor.getInstance().post( new DeleteFileEvent(downloadedFile.getFilePath())));
		want(downloadableId);
	}
	
	public void blackListSearchResult( String url ) {
		searchResultDAO.blacklist( url );
	}

	public void ignore(Downloadable downloadable) {
		DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.IGNORED, String.format("<a href='%s'>%s</a> is now ignored", downloadable.getRelativeLink(), downloadable.toString()) );
		BackLogProcessor.getInstance().unschedule( String.format( "task.downloadable.id == %d", downloadable.getId() ) );
	}

	public void suggest( Downloadable downloadable, String url ) {
		DownloadableManager.getInstance().logStatusChange( downloadable, DownloadableStatus.SUGGESTED );
		suggestionURLDAO.saveSuggestionURL(downloadable.getId(), url );
	}
	
	public void cancelDownload( long downloadableId ) {
		List<SearchResult> searchResults = searchResultDAO.findSearchResults( downloadableId );
		for (SearchResult searchResult : searchResults) {
			if (searchResult.isDownloaded() && StringUtils.isNotEmpty( searchResult.getClientId() )) {
				BackLogProcessor.getInstance().post( new CancelDownloadEvent( searchResult ));
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

	public void saveResult(String title, String url, Class<?> providerClass, String referer, float sizeInMegs,
			SearchResultType downloadType, long downloadableId) {
		
		searchResultDAO.save(url, providerClass, referer, sizeInMegs, title, downloadType, downloadableId, null);
		
	}

	public Set<DownloadableFile> getAllFiles( Class<? extends Downloadable> downloadableClass) {
		return downloadableDAO.getAllFiles( downloadableClass );
	}

	public List<DownloadableFile> getAllFiles(long downloadableId) {
		return downloadableDAO.getAllFiles( downloadableId );
	}

	public void clearBlackList() {
		searchResultDAO.clearBlackList();
	}
	
	public void saveDownloadLocations(long downloadableId, String title, Class<?> providerClass, String referer, float size, Collection<DownloadLocation> downloadLocations) {
		if (downloadableId >= 0 && downloadLocations != null && !downloadLocations.isEmpty()) {
			for (DownloadLocation downloadLocation : downloadLocations) {
				saveDownloadLocation(downloadableId, title, providerClass, referer, size, downloadLocation);
			}
		}		
	}
	
	public void saveDownloadLocation(long downloadableId, String title, Class<?> providerClass, String referer, float size, DownloadLocation downloadLocation) {
		if (downloadableId >= 0 && downloadLocation != null) {
			saveResult(title, downloadLocation.getUrl(), providerClass, referer, size, downloadLocation.getType(), downloadableId);
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
	
	public Class<? extends Downloadable> getDownloadableTypeBySimpleName( String simpleName ) {
		for (Class<? extends Downloadable> downloadableClass : downloadableTypes) {
			if (downloadableClass.getSimpleName().equalsIgnoreCase( simpleName )) {
				return downloadableClass;
			}
		}
		return null;
	}

	public List<DownloadInfo> findByStatus(Class<? extends Downloadable> klass, DownloadableStatus status) {
		return downloadableDAO.findByStatus(klass, status);
	}

	public void updateYear(long id, int year) {
		downloadableDAO.updateYear(id, year);
	}

	public void updateName(long id, String name) {
		downloadableDAO.updateName(id, name);
	}

	public List<DownloableCount> getCounts() {
		return downloadableDAO.getCounts();
	}
	
	public static Path resolveImage( Class<? extends Downloadable> downloadableClass, long downloadableId ) {
		// FIXME : do not hardcode .jpg ??
		return LocalImageCache.getInstance().resolveLocal( downloadableClass.getSimpleName() + "/" + downloadableId + ".jpg" );
	}

	public static Path resolveImage( Downloadable downloadable ) {
		return resolveImage(downloadable.getClass(), downloadable.getId() );
	}
	
	public static boolean hasImage( Downloadable downloadable ) throws IOException {
		return hasImage( downloadable.getClass(), downloadable.getId() );
	}
	
	public static boolean hasImage( Class<? extends Downloadable> downloadableClass, long downloadableId ) throws IOException {
		Path image = resolveImage(downloadableClass, downloadableId);
		return Files.exists( image ) && Files.size( image ) > 0;
	}
	
	public static boolean downloadImage( Downloadable downloadable, String url, String referer ) throws IOException {
		return downloadImage(downloadable.getClass(), downloadable.getId(), url, referer); 
	}
	
	public static boolean downloadImage( Downloadable downloadable, Path imageFile ) throws IOException {
		Path localFile = resolveImage(downloadable);
		Files.createDirectories( localFile.getParent() );
		Files.copy(imageFile, localFile, StandardCopyOption.REPLACE_EXISTING); 
		return true;
	}

	public static boolean downloadImage(Path localFile, String url, String referer ) throws IOException {
		if (url != null) {
			Files.createDirectories( localFile.getParent() );
			return HTTPClient.getInstance().downloadImage(url, referer, localFile );
		}
		return false;
	}	

	public static boolean downloadImage( Class<? extends Downloadable> downloadableClass, long downloadableId, String url, String referer ) throws IOException {
		Path localFile = resolveImage(downloadableClass, downloadableId);
		return downloadImage(localFile, url, referer);
	}

	public void deleteSuggestions(Class<? extends Downloadable> klass) {
		downloadableDAO.delete(klass, DownloadableStatus.SUGGESTED);
	}

}
