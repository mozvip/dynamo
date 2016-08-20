package dynamo.suggesters.music;

import dynamo.core.Labelized;

public interface MusicAlbumSuggester extends Labelized {

	public void suggestAlbums() throws Exception;

}
