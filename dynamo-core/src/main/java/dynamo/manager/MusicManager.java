package dynamo.manager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import core.RegExp;
import core.WebDocument;
import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.AudioFileFilter;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.backlog.tasks.files.FileUtils;
import dynamo.backlog.tasks.files.MoveFileTask;
import dynamo.backlog.tasks.music.FindMusicAlbumImageTask;
import dynamo.backlog.tasks.music.ImportMusicFolderTask;
import dynamo.core.Language;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.jdbi.MusicDAO;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicArtist;
import dynamo.model.music.MusicFile;
import dynamo.model.music.MusicQuality;
import dynamo.suggesters.RefreshMusicSuggestionsTask;
import dynamo.suggesters.music.MusicAlbumSuggester;
import hclient.HTTPClient;

public class MusicManager implements Reconfigurable {

	public final static String VARIOUS_ARTISTS = "Various Artists";
	public final static String ORIGINAL_SOUNDTRACK = "Original Soundtrack";

	@Configurable( category="Music", name="Enable Music", bold=true )
	private boolean enabled;

	@Configurable( category="Music", name="Default Metadata Language", defaultValue="EN", required="#{MusicManager.enabled}", disabled="#{!MusicManager.enabled}" )
	private Language metaDataLanguage;

	@Configurable( category="Music", name="Music Quality", required="#{MusicManager.enabled}", disabled="#{!MusicManager.enabled}" )
	private MusicQuality musicQuality = MusicQuality.COMPRESSED;

	@Configurable( category="Music", name="Music Folders", required="#{MusicManager.enabled}", disabled="#{!MusicManager.enabled}", contentsClass=Path.class )
	private List<Path> folders;

	@Configurable(category="Music", name="Music Providers", required="#{MusicManager.enabled}", disabled="#{!MusicManager.enabled}", contentsClass=MusicAlbumFinder.class )
	private List<MusicAlbumFinder> musicDownloadProviders;	

	@Configurable( category="Music", name="Clean music folders during scan (remove useless files like *.nfo, ...)", required="#{MusicManager.enabled}", disabled="#{!MusicManager.enabled}" )
	private boolean cleanDuringImport;
	
	@Configurable( category="Music", name="Music Album Suggesters", required="#{MusicManager.enabled}", disabled="#{!MusicManager.enabled}", contentsClass=MusicAlbumSuggester.class, ordered=false )
	private Collection<MusicAlbumSuggester> suggesters;

	private MusicDAO musicDAO = DAOManager.getInstance().getDAO( MusicDAO.class );
	private DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Language getMetaDataLanguage() {
		return metaDataLanguage;
	}

	public void setMetaDataLanguage(Language metaDataLanguage) {
		this.metaDataLanguage = metaDataLanguage;
	}

	public MusicQuality getMusicQuality() {
		return musicQuality;
	}

	public void setMusicQuality(MusicQuality musicQuality) {
		this.musicQuality = musicQuality;
	}

	public List<MusicAlbumFinder> getMusicDownloadProviders() {
		return musicDownloadProviders;
	}

	public void setMusicDownloadProviders( List<MusicAlbumFinder> musicDownloadProviders ) {
		this.musicDownloadProviders = musicDownloadProviders;
	}

	public List<Path> getFolders() {
		return folders;
	}

	public void setFolders(List<Path> folders) {
		this.folders = folders;
	}

	public boolean isCleanDuringImport() {
		return cleanDuringImport;
	}

	public void setCleanDuringImport(boolean cleanDuringImport) {
		this.cleanDuringImport = cleanDuringImport;
	}
	
	public Collection<MusicAlbumSuggester> getSuggesters() {
		return suggesters;
	}
	
	public void setSuggesters(Collection<MusicAlbumSuggester> suggesters) {
		this.suggesters = suggesters;
	}

	private MusicManager() {
	}

	static class SingletonHolder {
		static MusicManager instance = new MusicManager();
	}

	public static MusicManager getInstance() {
		return SingletonHolder.instance;
	}
	
	private static String[] variousArtists = new String[] {
		"Artistes Divers ", "Various Artists", "Multi-Artistes", "Various"
	};
	
	private static String[] originalSoundTrackArtists = new String[] {
		"Soundtrack", "Original Soundtrack"
	};
	
	private static String[] soundtrackDetectors = new String[] {
		".*Music From the Motion Picture.*",
		".*Original Motion Picture Soundtrack.*",
	};
	
	public static boolean isOriginalSountrack( String albumName ) {
		for (String soundtrackDetector : soundtrackDetectors) {
			if (RegExp.matches( albumName, soundtrackDetector)) {
				return true;
			}
		}
		return false;
	}

	private static String[] nameExtractors = new String[] {
		"(.*)\\s+\\(Edition limit.*\\)",
		"(.*)\\s+\\(Music From the Motion Picture\\)",
		"(.*)\\s+\\(Original Motion Picture Soundtrack\\)",
		"(.*)\\s+\\(.*\\s+Edition\\)",
		"(.*)\\s+-\\s*Coffret\\s+.*",
		"(.*)\\s+\\(Coffret\\s+.*",
		"(.*)\\s+[\\(\\[]Deluxe\\s+\\w+.*",
		"(.*)\\s+[\\(\\[]Deluxe\\s*[\\)\\]]",
		"(.*)\\s+[\\(\\[]Digipack\\s*[\\)\\]]",
		"(.*)\\s+[\\(\\[]LP[\\)\\]]",
		"(.*)\\s+-\\s+Edition\\s+Limit.*",
		"(.*)\\s+\\(CD\\s+\\+\\s+Livre\\)",
		"(.*)\\s+\\[CD\\+DVD\\]",
		"(.*)\\s*\\[LP\\]",
		"(.*)\\s+\\(DI[Ss]C\\s*\\d+\\)",
		"(.*)\\s+DI[Ss]C\\s*\\d+",
		"(.*)\\s+\\(CD\\s*\\d+\\)",
		"(.*)\\s+CD\\s*\\d+",
		"(.*) \\(\\d+\\s+CD\\)",
		"(.*)\\s+\\(Boitier\\s+\\w+\\)",
		"(.*)\\s*-\\s*Edition\\s+Collector.*",
		"(.*)\\s*-\\s*Edition\\s+Deluxe.*",
		"(.*)\\s*-\\s*Edition\\s+[Ll]imit.*",
		"(.*)\\s*\\[Deluxe\\s+Edition\\]",
		"(.*)\\s*\\[Limited\\s+Edition.*\\]",
		"(.*)\\s*\\[Ltd\\.\\s+Edition.*\\]",
		"(.*)\\s*\\[Deluxe\\]",
		"(.*)\\s*\\(\\d+\\s+CD\\)",		
		"(.*)\\s*[\\(\\[]Deluxe.*Edition.*[\\)\\]]",
		"(.*)\\s*[\\(\\[]Deluxe [Vv]ersion.*[\\)\\]]",
		"(.*)\\s+: Special Edition",
		"(.*)\\s+\\[VINYL\\]",
		"(.*)\\s+\\(Bonus \\w+ Version\\)",
		"(.*)\\s+-\\s+EP",
		"(.*)\\s+\\(EP\\)"
	};	
	
	private static String[] regExpArtistsName = new String[] {
		"~\\s*(.*)",
		"(.*)\\s*[\\(\\[]Artist[\\)\\]]"
	};
	
	Cache<String, MusicAlbum> albumCache = CacheBuilder.newBuilder().maximumSize(1000).build();	
	Cache<String, MusicArtist> artistCache = CacheBuilder.newBuilder().maximumSize(1000).build();
	
	public static String getArtistName( String artistName ) {
		String rawArtistName = artistName.trim();
		for (String soundtrackArtistToken : originalSoundTrackArtists) {
			if (rawArtistName.equalsIgnoreCase( soundtrackArtistToken )) {
				rawArtistName = ORIGINAL_SOUNDTRACK;
				break;
			}
		}
		for (String variousArtistToken : variousArtists) {
			if (rawArtistName.equalsIgnoreCase( variousArtistToken )) {
				rawArtistName = VARIOUS_ARTISTS;
				break;
			}
		}
		for (String regexp : regExpArtistsName) {
			rawArtistName = RegExp.filter(rawArtistName, regexp);	
		}
		return rawArtistName.trim();
	}	

	public static String getAlbumName( String album ) {
		return RegExp.clean(album, nameExtractors);
	}

	public static String getSearchString( String artistName, String name ) {
		
		if (artistName == null) {
			artistName = "Unknown Artist";
		} else {
			for (String regexp : regExpArtistsName) {
				artistName = RegExp.filter(artistName, regexp);	
			}
			artistName = artistName.toUpperCase().trim();
			artistName = RegExp.filter(artistName, "THE\\s+(.*)");
			artistName = RegExp.filter(artistName, "(.*),\\s*THE");
		}
		
		String albumName = getAlbumName( name );
		String searchString = String.format("%s%s", artistName, albumName).toUpperCase().trim();
		searchString = searchString.replaceAll("[\\W]", "");
		return searchString;
	}
	
	public MusicAlbum getAlbum( String searchString ) {
		return musicDAO.findBySearchString( searchString );
	}	
	
	public MusicAlbum getAlbum( String artistName, String albumName ) {
		String searchString = getSearchString( artistName, albumName );
		return getAlbum(searchString);

	}
	
	public void suggest( String artistName, String albumName, String genre, String imageURL, String referer) throws MalformedURLException, ExecutionException {
		String image = LocalImageCache.getInstance().download("albums", MusicManager.getSearchString(artistName, albumName), imageURL, referer);
		getAlbum(artistName, albumName, genre, image, DownloadableStatus.SUGGESTED, null, musicQuality, true);
	}

	public synchronized MusicAlbum getAlbum( String artistName, String albumName, String genre, String image, DownloadableStatus status, Path path, MusicQuality quality, boolean createIfMissing ) throws ExecutionException {
		
		MusicAlbum album = null;

		if (StringUtils.isBlank( albumName )) {
			albumName = "<Unknown>";
		}
		
		MusicArtist artist = getArtist( artistName, true );

		// clean album name
		albumName = getAlbumName( albumName );
		
		String searchString = getSearchString(artist.getName(), albumName);
		
		album = musicDAO.findBySearchString(searchString);
		if ( album != null && album.getCoverImage() == null ) {
			BackLogProcessor.getInstance().schedule( new FindMusicAlbumImageTask( album ), false );
		}
		
		if (album != null) {
			if ( status == DownloadableStatus.DOWNLOADED && album.getStatus() != status) {
				DownloadableManager.getInstance().updateStatus(album, status);
			}
		}

		if (path == null) {
			path = getPath( artistName, albumName );
		}
		
		if (album == null && createIfMissing ) {
			album = new MusicAlbum(
					DownloadableManager.getInstance().createDownloadable(MusicAlbum.class, albumName, path, image, status),
					status, path, image, null, 
					artist.getName(), albumName, null, quality, null
			);
			musicDAO.save(album.getId(), artist.getName(), null, genre, quality, searchString);
			if (image == null) {
				BackLogProcessor.getInstance().schedule( new FindMusicAlbumImageTask( album ), false );
			}
		}

		if (album != null && album.getPath() == null && path != null) {
			downloadableDAO.updatePath(album.getId(), path);
		}

		return album;
	}

	public synchronized MusicArtist getArtist( String albumArtist, boolean createIfMissing ) {

		if (albumArtist == null || StringUtils.isBlank( albumArtist )) {
			albumArtist = "Unknown Artist";
		}

		String albumArtistWithoutThe = RegExp.extract( albumArtist, "[Tt][Hh]E\\s+(.*)" );
		if (albumArtistWithoutThe == null) {
			albumArtistWithoutThe = RegExp.extract( albumArtist, "(.*),?\\s+[Tt][Hh]E" );
		}

		albumArtist = albumArtistWithoutThe != null ?  albumArtistWithoutThe + ",The" : albumArtist; // FIXME: don't harcode this format (Artist,The)

		albumArtist = getArtistName( albumArtist );

		MusicArtist artist = musicDAO.findArtist( albumArtist );	
		if (artist == null) {
			if (createIfMissing) {

				List<String> aka = new ArrayList<String>();
				aka.add( albumArtist.toUpperCase() );
				if ( albumArtistWithoutThe != null ) {
					aka.add( albumArtistWithoutThe.toUpperCase() );
					aka.add( (albumArtistWithoutThe + ", The").toUpperCase());
					aka.add( (albumArtistWithoutThe + ",The").toUpperCase());
				}
				String[] split = albumArtist.split(" ");
				if (split.length == 2) {
					aka.add( (split[1] + " " + split[0]).toUpperCase() );
					aka.add( (split[1] + ", " + split[0]).toUpperCase() );
					aka.add( (split[1] + "," + split[0]).toUpperCase() );
				}

				String akas = StringUtils.join( aka, ';');
				artist = new MusicArtist( albumArtist, false, false, null, akas );

				musicDAO.createArtist( artist.getName(), artist.getAllMusicURL(), artist.isBlackListed(), artist.isFavorite(), artist.getAka());
			}
		}
		
		return artist;
		
	}

	public Path getPath( String albumArtist, String album ) {
		return getPath( FileUtils.getFolderWithMostUsableSpace( getFolders() ), albumArtist, album );
		
	}
	
	public String cleanForFileName( String string ) {
		string = string.replace(':', '-');
		string = string.replace('"', '_');	// FIXME
		string = string.replace('/', '-');
		string = string.replace('<', '[');
		string = string.replace('>', ']');
		string = string.replace('?', ' ');
		string = string.replace('*', '_');
		
		string = RegExp.filter( string, "(.*)[^\\.]+\\.+");

		string = string.trim();

		return string;
	}

	protected Path getPath( Path folder,  String artistName, String albumName) {
		return folder.resolve( cleanForFileName( artistName ) ).resolve( cleanForFileName( albumName ));
	}

	public String getAllMusicURL( String artistName ) throws IOException, URISyntaxException {

		String searchArtistURL = "http://www.allmusic.com/search/artists/" + URLEncoder.encode( artistName, "UTF-8");

		WebDocument searchDocument = HTTPClient.getInstance().getDocument(searchArtistURL, HTTPClient.REFRESH_ONE_WEEK);
		if (searchDocument != null) {

			Elements artistNameNodes = searchDocument.evaluateJSoup(".results li .name a");					

			Element selectedLinkElement = null;
			for (Element element : artistNameNodes) {
				String currentArtistName = element.ownText();
				if (StringUtils.equalsIgnoreCase( currentArtistName, artistName)) {
					selectedLinkElement = element;
					break;
				}
				if (StringUtils.containsIgnoreCase( currentArtistName, artistName)) {
					selectedLinkElement = element;
				}
			}
			if (selectedLinkElement != null) {
				return selectedLinkElement.attr("abs:href");
			}

		}
		
		return null;
	}

	public List<MusicAlbum> getSuggestedAlbums( String artistsSearchFilter ) {
		return musicDAO.find( artistsSearchFilter, DownloadableStatus.SUGGESTED );
	}

	public List<MusicAlbum> getWanted( String artistsSearchFilter) {
		List<MusicAlbum> albums = musicDAO.find( artistsSearchFilter, DownloadableStatus.WANTED );
		albums.addAll( musicDAO.find( artistsSearchFilter, DownloadableStatus.SNATCHED ) );
		return albums;
	}

	public List<MusicAlbum> getCollection( String artistsSearchFilter) {
		return musicDAO.find( artistsSearchFilter, DownloadableStatus.DOWNLOADED );
	}

	@Override
	public void reconfigure() {
		if (enabled) {
			for (Path path : getFolders()) {
				BackLogProcessor.getInstance().schedule( new ImportMusicFolderTask( path, true ) );
			}
		} else {
			BackLogProcessor.getInstance().unschedule( ImportMusicFolderTask.class );
			BackLogProcessor.getInstance().unschedule( RefreshMusicSuggestionsTask.class );
		}
	}

	public void cleanFolder(Path folder) throws IOException {
		// clean folder contents
		
		DirectoryStream<Path> ds = Files.newDirectoryStream(folder);
		List<Path> toDelete = new ArrayList<Path>();
		boolean canDeleteFiles = true;
		boolean emptyFolder = true;
		for (Path p : ds) {
			emptyFolder = false;
			
			if (Files.isDirectory(p) || AudioFileFilter.getInstance().accept( p )) {
				canDeleteFiles = false;
				break;
			}
			
			String fileName = p.getFileName().toString();
			if ( Files.size(p) == 0 || fileName.endsWith(".txt") || fileName.endsWith(".jpg") || fileName.endsWith(".m3u") || fileName.endsWith(".lnk") || fileName.endsWith(".sfv") || fileName.endsWith(".nfo") || fileName.equalsIgnoreCase("thumbs.db") || fileName.equalsIgnoreCase("desktop.ini") ) {
				toDelete.add( p );
			}
		}
		
		if (emptyFolder) {
			BackLogProcessor.getInstance().schedule( new DeleteTask( folder, true ));
		} else if (canDeleteFiles && !toDelete.isEmpty()) {
			for (Path path : toDelete) {
				BackLogProcessor.getInstance().schedule(new DeleteTask( path, true ), false);
			}
		}
	}

	public MusicArtist findArtist(String name) {
		return musicDAO.findArtist( name );
	}

	public void save(MusicArtist artist) {
		// TODO Auto-generated method stub
		
	}

	public List<MusicArtist> getArtists(String filter) {
		return musicDAO.findArtists( filter );
	}

	public List<MusicAlbum> getDownloadedAlbums(String artistsSearchFilter, int start, int limit) {
		return musicDAO.getDownloadedAlbums( artistsSearchFilter, start, limit );
	}

	public List<MusicAlbum> getAlbumsForArtist( String artistName ) {
		return musicDAO.findAllAlbumsForArtist(artistName);
	}

	public List<MusicAlbum> getAlbumsToDisplayForArtist( String artistName ) {
		return musicDAO.findAllAlbumsToDisplayForArtist(artistName);
	}

	public List<MusicFile> findMusicFiles(long albumId) {
		return musicDAO.findMusicFiles(albumId);
	}

	public MusicAlbum findAlbumById( long albumId ) {
		return musicDAO.find(albumId);
	}

	public List<MusicFile> findMusicFiles(String artistName) {
		return musicDAO.findMusicFiles(artistName);
	}

	public int getDownloadedAlbumsCount(String artistsSearchFilter) {
		return musicDAO.getDownloadedAlbumsCount(artistsSearchFilter);
	}

	public int getMusicFilesCount(String artistsSearchFilter, String albumNameFilter) {
		return musicDAO.getMusicFilesCount(artistsSearchFilter, albumNameFilter);
	}

	public List<MusicFile> getMusicFiles(String artistsSearchFilter, String albumNameFilter, int start, int count) {
		return musicDAO.getMusicFiles(artistsSearchFilter, albumNameFilter, start, count);
	}

	public List<MusicAlbum> findAlbumsWithImage() {
		return musicDAO.findAlbumsWithImage();
	}

	public int getMusicFilesCount(long albumId) {
		return musicDAO.getMusicFilesCount( albumId );
	}

}
