package dynamo.webapps.theaudiodb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AudioDBResponse {

	@JsonProperty(value="album")
	private List<AudioDBAlbum> albums;

	public List<AudioDBAlbum> getAlbums() {
		return albums;
	}

	public void setAlbums(List<AudioDBAlbum> albums) {
		this.albums = albums;
	}

}
