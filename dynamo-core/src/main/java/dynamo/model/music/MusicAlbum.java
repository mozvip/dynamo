package dynamo.model.music;

import java.nio.file.Path;
import java.util.Date;

import dynamo.manager.MusicManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;

public class MusicAlbum extends Downloadable {

	private String searchString;
	private MusicQuality quality = MusicQuality.COMPRESSED;
	private String artistName;
	private String genre;
	private Path folder;
	private Long tadbAlbumId;
	
	// empty constructor for JSON
	public MusicAlbum() {
		super();
	}

	public MusicAlbum( Long id,  String albumName, String label, DownloadableStatus status, int year, Date creationDate, Path folder, String aka, String artistName, String genre, MusicQuality quality, Long tadbAlbumId) {
		super( id, albumName, label, status, aka, year, creationDate );
		this.artistName = artistName;
		this.genre = genre;
		this.tadbAlbumId = tadbAlbumId;
		this.quality = quality;
		this.folder = folder;

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
		return String.format("index.html#/music-album/%d", getId());
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

	public Path getFolder() {
		return folder;
	}
	
	public Long getTadbAlbumId() {
		return tadbAlbumId;
	}
	
	@Override
	public Path determineDestinationFolder() {
		if ( folder != null ) {
			return folder;
		} else {
			return MusicManager.getInstance().getPath(getArtistName(), getName() );
		}
	}

}
