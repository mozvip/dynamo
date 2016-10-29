package dynamo.webapps.theaudiodb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AudioDBResponse {

	@JsonProperty(value = "album")
	private List<AudioDBAlbum> album;

	@JsonProperty(value = "artists")
	private List<AudioDBArtist> artists;

	public List<AudioDBAlbum> getAlbum() {
		return album;
	}

	public void setAlbum(List<AudioDBAlbum> album) {
		this.album = album;
	}

	public List<AudioDBArtist> getArtists() {
		return artists;
	}

	public void setArtists(List<AudioDBArtist> artists) {
		this.artists = artists;
	}

}
