package dynamo.finders;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.model.result.SearchResult;
import dynamo.providers.KATProvider;


public class KATTorrentFinderTest {
	
	static KATProvider finder = new KATProvider();
	
	@BeforeClass
	public static void init() {
		finder.setEnabled( true );
		finder.reconfigure();
	}

	@Test
	public void testFindDownloadsForEpisode() throws Exception {
		
		List<SearchResult> results = finder.findDownloadsForEpisode("Game of Thrones", Language.EN, 3, 4);
		for (SearchResult searchResult : results) {
			finder.download( searchResult.getUrl(), searchResult.getReferer() );
			System.out.println( searchResult );
		}
	}

}
