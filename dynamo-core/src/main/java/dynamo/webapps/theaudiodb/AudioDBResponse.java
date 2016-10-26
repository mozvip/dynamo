package dynamo.webapps.theaudiodb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AudioDBResponse {

	@JsonProperty(value="album")
	private List<AudioDBAlbum> album;

	public List<AudioDBAlbum> getAlbum() {
		return album;
	}

	public void setAlbum(List<AudioDBAlbum> album) {
		this.album = album;
	}

}
