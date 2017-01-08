package com.github.dynamo.finders.music;

import java.util.List;

import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;


public interface MusicAlbumFinder {
	
	public List<SearchResult> findMusicAlbum( String artist, String album, MusicQuality quality ) throws MusicAlbumSearchException;

}
