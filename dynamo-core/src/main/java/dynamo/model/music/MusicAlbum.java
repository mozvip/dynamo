package dynamo.model.music;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;

import dynamo.core.manager.ErrorManager;
import dynamo.manager.MusicManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;

public class MusicAlbum extends Downloadable {

	private String searchString;
	private MusicQuality quality = MusicQuality.COMPRESSED;
	private String allMusicURL;
	private String artistName;
	private String genre;

	public MusicAlbum(  Long id, DownloadableStatus status, Path path, String coverImage, String aka, String artistName, String albumName, String genre, MusicQuality quality, String allMusicURL) {
		super( id, albumName, status, path, coverImage, aka, null );
		this.artistName = artistName;
		this.genre = genre;
		this.allMusicURL = allMusicURL;
		this.quality = quality;
		
		this.searchString = MusicManager.getSearchString( artistName, albumName );
	}

	public String getSearchString() {
		return searchString;
	}

	public MusicQuality getQuality() {
		return quality;
	}
	public void setQuality(MusicQuality quality) {
		this.quality = quality;
	}

	public String getArtistName() {
		return artistName;
	}

	public String getAllMusicURL() {
		return allMusicURL;
	}

	public void setAllMusicURL(String allMusicURL) {
		this.allMusicURL = allMusicURL;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", artistName, getName());
	}
	
	@Override
	public String getRelativeLink() {
		try {
			return String.format("music-artist.jsf?name=%s#%d", URLEncoder.encode(artistName, "UTF-8"), getId());
		} catch (UnsupportedEncodingException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		return "music.jsf";
	}
	
	@Override
	public int hashCode() {
		return getSearchString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MusicAlbum) {
			return ((MusicAlbum)obj).getSearchString().equals( getSearchString() );
		}
		return false;
	}

	@Override
	public Path determineDestinationFolder() {
		if ( getPath() != null ) {
			return getPath();
		} else {
			return MusicManager.getInstance().getPath(getArtistName(), getName() );
		}
	}

}
