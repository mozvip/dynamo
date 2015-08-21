package dynamo.finders;

import java.util.List;

import org.junit.Test;

import dynamo.core.Language;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.providers.T411Provider;
import dynamo.tests.AbstractDynamoTest;


public class T411TorrentFinderTest extends AbstractDynamoTest {
	
	T411Provider finder = new T411Provider();

	@Test
	public void testFindDownloadsForEpisode() throws Exception {
		List<SearchResult> results = finder.findDownloadsForEpisode("Game of Thrones", Language.EN, 3, 4);
		for (SearchResult searchResult : results) {
			System.out.println( searchResult );
		}
	}
	
	@Test
	public void testFindDownloadsForSeason() throws Exception {
		List<SearchResult> results = finder.findDownloadsForSeason("Awkward", Language.EN, 2);
		for (SearchResult searchResult : results) {
			System.out.println( searchResult );
		}		
	}

	@Test
	public void testFindMusicAlbum() throws Exception {
		List<SearchResult> results = finder.findMusicAlbum("Madonna", "MDNA", MusicQuality.LOSSLESS);
		for (SearchResult searchResult : results) {
			System.out.println( searchResult );
		}		
	}
	
	@Test
	public void testSuggestMovies() throws Exception {
		finder.suggestMovies();
	}

}
