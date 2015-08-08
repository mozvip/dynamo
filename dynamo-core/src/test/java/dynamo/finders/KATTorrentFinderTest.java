package dynamo.finders;

import java.util.List;

import org.junit.Test;

import dynamo.core.Language;
import dynamo.model.result.SearchResult;
import dynamo.providers.KATProvider;


public class KATTorrentFinderTest {
	
	KATProvider finder = new KATProvider();

	@Test
	public void testFindDownloadsForEpisode() throws Exception {
		List<SearchResult> results = finder.findDownloadsForEpisode("Game of Thrones", Language.EN, 3, 4);
		for (SearchResult searchResult : results) {
			System.out.println( searchResult );
		}
	}

}
