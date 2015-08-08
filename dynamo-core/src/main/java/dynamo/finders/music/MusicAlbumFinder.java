package dynamo.finders.music;

import java.util.List;

import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;


public interface MusicAlbumFinder {
	
	public List<SearchResult> findMusicAlbum( String artist, String album, MusicQuality quality ) throws MusicAlbumSearchException;

}
