package dynamo.providers;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.model.result.SearchResult;


public class KATTorrentFinderTest extends AbstractProviderTest {
	
	static KATProvider finder;
	
	@BeforeClass
	public static void initTest() throws Exception {
		ConfigValueManager.mockConfiguration("KATProvider.enabled", true);
		finder = (KATProvider) DynamoObjectFactory.getInstance( KATProvider.class );
	}

	@Test
	public void testFindDownloadsForEpisode() throws Exception {
		
		List<SearchResult> results = finder.findDownloadsForEpisode("Game of Thrones", createMockedSeries("Game of Thrones", Language.EN), 3, 4);
		for (SearchResult searchResult : results) {
			finder.download( searchResult.getUrl(), searchResult.getReferer() );
			System.out.println( searchResult );
		}
	}

}
