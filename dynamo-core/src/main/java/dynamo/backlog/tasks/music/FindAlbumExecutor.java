package dynamo.backlog.tasks.music;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.tasks.core.FindDownloadableExecutor;
import dynamo.core.DownloadFinder;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.MusicManager;
import dynamo.model.Downloadable;
import dynamo.model.backlog.find.FindMusicAlbumTask;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;

public class FindAlbumExecutor extends FindDownloadableExecutor {
	
	private MusicAlbum musicAlbum;
	
	private final static int MINIMUM_SIZE = 35;
	
	public FindAlbumExecutor(FindMusicAlbumTask item, SearchResultDAO searchResultDAO ) {
		super( item, searchResultDAO );
		musicAlbum = (MusicAlbum) item.getDownloadable();
	}

	@Override
	public Collection<String> getWordsBlackList(Downloadable downloadable) {
		// TODO
		return null;
	}
	
	@Override
	public int evaluateResult( SearchResult result ) {
		int score = 0;
		
		if ( StringUtils.containsIgnoreCase( result.getTitle(), "FLAC") ) {
			if (musicAlbum.getQuality() == MusicQuality.LOSSLESS) {
				score += 5;
			} else {
				score = -1;
			}
		}
		
		if ( StringUtils.containsIgnoreCase( result.getTitle(), "MP3") || StringUtils.containsIgnoreCase( result.getTitle(), "CBR") ||StringUtils.containsIgnoreCase( result.getTitle(), "320kbps") || StringUtils.containsIgnoreCase( result.getTitle(), "256kbps") || StringUtils.containsIgnoreCase( result.getTitle(), "192kbps") ) {
			if (musicAlbum.getQuality() == null || musicAlbum.getQuality() == MusicQuality.COMPRESSED) {
				score += 5;
			} else {
				score = -1;
			}
		}

		return score;
	}
	
	@Override
	public List<?> getProviders() {
		return MusicManager.getInstance().getMusicDownloadProviders();
	}
	
	@Override
	public List<SearchResult> getResults(DownloadFinder finder, Downloadable downloadable) {
		MusicAlbumFinder musicAlbumFinder =  (MusicAlbumFinder) finder;
		List<SearchResult> allResults = new ArrayList<SearchResult>();
		try {
			
			String albumName = musicAlbum.getAlbum();
			albumName = albumName.replace('/', ' ');
			
			Set<String> albumNames = new HashSet<>();
			albumNames.add( albumName.replace("+", "plus") );
			albumNames.add( albumName.replace('/', ' ') );
			albumNames.add( albumName );
			
			for (String album : albumNames) {
				List<SearchResult> results = musicAlbumFinder.findMusicAlbum( musicAlbum.getArtistName(), album, musicAlbum.getQuality() );
				if (results != null && !results.isEmpty()) {
					allResults.addAll( results );
				} else if ("Various Artists".equals(musicAlbum.getArtistName())) {
					results = musicAlbumFinder.findMusicAlbum( "", albumName, musicAlbum.getQuality() );
					if (results != null && !results.isEmpty()) {
						allResults.addAll( results );
					}
				}
			}
			
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( getTask(), e );
		}
		
		for (Iterator<SearchResult> iterator = allResults.iterator(); iterator.hasNext();) {
			SearchResult searchResult = iterator.next();
			if (searchResult.getSizeInMegs() < MINIMUM_SIZE) {
				iterator.remove();
			}
		}
		
		return allResults;
	}

}
