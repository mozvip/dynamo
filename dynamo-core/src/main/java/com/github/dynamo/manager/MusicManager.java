package com.github.dynamo.manager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.music.ScanMusicFolderTask;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.finders.music.MusicAlbumFinder;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicArtist;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.music.TheAudioDb;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;
import com.github.dynamo.suggesters.RefreshMusicSuggestionsTask;
import com.github.dynamo.suggesters.music.MusicAlbumSuggester;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;
import com.github.mozvip.theaudiodb.model.AudioDbAlbum;
import com.github.mozvip.theaudiodb.model.AudioDbResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class MusicManager implements Reconfigurable {

	public final static String VARIOUS_ARTISTS = "Various Artists";
	public final static String ORIGINAL_SOUNDTRACK = "Original Soundtrack";

	@Configurable
	private MusicQuality musicQuality = MusicQuality.COMPRESSED;

	@Configurable(contentsClass=Path.class )
	private List<Path> folders;

	@Configurable
	private Path downloadFolder;

	@Configurable( contentsClass=MusicAlbumFinder.class )
	private List<MusicAlbumFinder> musicDownloadProviders;	
	
	@Configurable( contentsClass=MusicAlbumSuggester.class, ordered=false )
	private Collection<MusicAlbumSuggester> suggesters;

	private MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );

	public boolean isEnabled() {
		return folders != null && folders.size() > 0;
	}

	public MusicQuality getMusicQuality() {
		return musicQuality;
	}

	public void setMusicQuality(MusicQuality musicQuality) {
		this.musicQuality = musicQuality;
	}
	
	public Path getDownloadFolder() {
		return downloadFolder;
	}
	
	public void setDownloadFolder(Path downloadFolder) {
		this.downloadFolder = downloadFolder;
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
		"(.*)\\s+\\(.dition limit.*\\)",
		"(.*)\\s+\\(explicit\\)",
		"(.*)\\s+\\(edited\\)",
		"(.*)\\s+\\(\\d+CD\\)",
		"(.*)\\s+\\(Music From the Motion Picture\\)",
		"(.*)\\s+\\(Original Motion Picture Soundtrack\\)",
		"(.*)\\s+\\(.*\\s+Edition\\)",
		"(.*)\\s+-\\s*Coffret\\s+.*",
		"(.*)\\s+\\(Coffret\\s+.*",
		"(.*)\\s+[\\(\\[]Deluxe\\s+\\w+.*",
		"(.*)\\s+[\\(\\[]Deluxe\\s*[\\)\\]]",
		"(.*)\\s+[\\(\\[]Digipack\\s*[\\)\\]]",
		"(.*)\\s+[\\(\\[]LP[\\)\\]]",
		"(.*)\\s+-\\s+.dition\\s+Limit.*",
		"(.*)\\s+\\(CD\\s+\\+\\s+Livre\\)",
		"(.*)\\s+\\[CD\\+DVD\\]",
		"(.*)\\s*\\[LP\\]",
		"(.*)\\s+\\(DIsC\\s*\\d+\\)",
		"(.*)\\s+DIsC\\s*\\d+",
		"(.*)\\s+\\(CD\\s*\\d+\\)",
		"(.*)\\s+CD\\s*\\d+",
		"(.*) \\(\\d+\\s+CD\\)",
		"(.*)\\s+\\(Boitier\\s+\\w+\\)",
		"(.*)\\s*-\\s*.dition\\s+Collector.*",
		"(.*)\\s*-\\s*.dition\\s+Deluxe.*",
		"(.*)\\s*-\\s*.dition\\s+[Ll]imit.*",
		"(.*)\\s*\\[Deluxe\\s+.dition\\]",
		"(.*)\\s*\\[Limited\\s+.dition.*\\]",
		"(.*)\\s*\\[Ltd\\.\\s+.dition.*\\]",
		"(.*)\\s*\\[Deluxe\\]",
		"(.*)\\s*\\(\\d+\\s+CD\\)",		
		"(.*)\\s*[\\(\\[]Deluxe.*.dition.*[\\)\\]]",
		"(.*)\\s*[\\(\\[]Deluxe version.*[\\)\\]]",
		"(.*)\\s+: Special Edition",
		"(.*)\\s+\\[VINYL\\]",
		"(.*)\\s+\\(Bonus \\w+ Version\\)",
		"(.*)\\s+-\\s+EP",
		"(.*)\\s+-\\s*",
		"(.*)\\s+\\(EP\\)"
	};	
	
	private static String[] regExpArtistsName = new String[] {
		"~\\s*(.*)",
		"(.*)\\s*[\\(\\[]Artist[\\)\\]]"
	};
	
	Cache<String, MusicAlbum> albumCache = CacheBuilder.newBuilder().maximumSize(1000).build();	
	Cache<String, MusicArtist> artistCache = CacheBuilder.newBuilder().maximumSize(1000).build();
	
	public static String getArtistName( String artistName ) {
		if (StringUtils.isBlank( artistName )) {
			artistName = "Unknown Artist";
		}
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
		
		// replace "Beatles, The" by "The Beatles"
		String albumArtistWithoutThe = RegExp.extract( rawArtistName, "(.*),\\s*the" );
		if (albumArtistWithoutThe != null) {
			rawArtistName = "The " + albumArtistWithoutThe;
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
		searchString = searchString.replaceAll("[\\s-,\\.:<>]", "");
		return searchString;
	}
	
	public MusicAlbum getAlbum( String searchString ) {
		return musicDAO.findBySearchString( searchString );
	}	
	
	public MusicAlbum getAlbum( String artistName, String albumName ) {
		String searchString = getSearchString( artistName, albumName );
		return getAlbum(searchString);

	}
	
	public void suggest( String artistName, String albumName, String imageURL, String referer, String suggestionURL) throws ExecutionException, IOException {
		MusicAlbum album = getAlbum(artistName, albumName, DownloadableStatus.SUGGESTED, null, musicQuality);
		if (imageURL != null) {
			DownloadableManager.downloadImage(album, imageURL, referer);
		}
		if (suggestionURL != null) {
			DownloadableManager.getInstance().saveSuggestionURL(album.getId(), suggestionURL);
		}
	}

	public synchronized MusicAlbum getAlbum( String artistName, String albumName, DownloadableStatus status, Path folder, MusicQuality quality ) throws ExecutionException, IOException {
		
		MusicAlbum album = null;

		if (StringUtils.isBlank( albumName )) {
			albumName = "<Unknown>";
		}
		
		MusicArtist artist = getOrCreateArtist( artistName );

		// clean album name
		albumName = getAlbumName( albumName );
		
		String searchString = getSearchString(artist.getName(), albumName);
		
		album = musicDAO.findBySearchString(searchString);
		if ( album != null ) {
			if ( status == DownloadableStatus.DOWNLOADED && album.getStatus() != status) {
				DownloadableManager.getInstance().updateStatus(album, status);
			}
		} else {
			
			AudioDbAlbum audioDBAlbum = null;
			int year = -1;
			Optional<AudioDbResponse> optAlbum = TheAudioDb.getInstance().searchAlbum(artistName, albumName);
			if (optAlbum.isPresent() && optAlbum.get().getAlbum() != null) {
				audioDBAlbum = optAlbum.get().getAlbum().get( 0 );
				year = audioDBAlbum.getIntYearReleased();
			}

			album = new MusicAlbum(
					DownloadableManager.getInstance().createDownloadable(MusicAlbum.class, albumName, year, status),
					albumName, null, 
					status, year, new Date(), folder, null, 
					artist.getName(), null, quality, audioDBAlbum != null ? audioDBAlbum.getIdAlbum() : null
			);
			musicDAO.save(album.getId(), artist.getName(), audioDBAlbum != null ? audioDBAlbum.getIdAlbum() : null, audioDBAlbum != null ? audioDBAlbum.getStrGenre() : null, quality, searchString, folder);
		}
		
		return album;
	}

	public synchronized MusicArtist getOrCreateArtist( String albumArtist ) {

		albumArtist = getArtistName( albumArtist );

		MusicArtist artist = musicDAO.findArtist( albumArtist );	
		if (artist == null) {
			
			Long tadbArtistId = null;
			try {
				Optional<AudioDbResponse> searchResult = TheAudioDb.getInstance().searchArtist( albumArtist );
				if (searchResult.isPresent() && searchResult.get().getArtists() != null) {
					tadbArtistId = searchResult.get().getArtists().get(0).getIdArtist();
				}
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}	

			List<String> aka = new ArrayList<String>();
			aka.add( albumArtist.toUpperCase() );
			String albumArtistWithoutThe = RegExp.extract( albumArtist, "(.+)\\s+,the");
			if ( albumArtistWithoutThe != null ) {
				aka.add( albumArtistWithoutThe.toUpperCase() );
			}
			albumArtistWithoutThe = RegExp.extract( albumArtist, "the\\s+(.+)");
			if ( albumArtistWithoutThe != null ) {
				aka.add( albumArtistWithoutThe.toUpperCase() );
			}
			String[] split = albumArtist.split(" ");
			if (split.length == 2) {
				aka.add( (split[1] + " " + split[0]).toUpperCase() );
				aka.add( (split[1] + ", " + split[0]).toUpperCase() );
				aka.add( (split[1] + "," + split[0]).toUpperCase() );
			}

			String akas = StringUtils.join( aka, ';');
			artist = new MusicArtist( albumArtist, false, false, null, akas );

			musicDAO.createArtist( artist.getName(), tadbArtistId, artist.isBlackListed(), artist.isFavorite(), artist.getAka());

		}
		
		return artist;
		
	}

	public Path getDownloadPath( String albumArtist, String album ) {
		return getPath( downloadFolder, getArtistName(albumArtist), album );
		
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
		if (isEnabled()) {
			for (Path path : getFolders()) {
				BackLogProcessor.getInstance().schedule( new ScanMusicFolderTask( path ), false );
			}
		} else {
			BackLogProcessor.getInstance().unschedule( ScanMusicFolderTask.class );
			BackLogProcessor.getInstance().unschedule( RefreshMusicSuggestionsTask.class );
		}
	}

	public MusicArtist findArtist(String name) {
		return musicDAO.findArtist( name );
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

	public List<MusicAlbum> findAlbumsWithImage() {
		return musicDAO.findAlbumsWithImage();
	}

}
