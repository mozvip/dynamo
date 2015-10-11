package dynamo.manager;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.manager.ConfigValueManager;
import dynamo.model.result.SearchResult;
import dynamo.providers.KATProvider;

public class FinderManagerTest {
	
	@BeforeClass
	public static void init() {
		ConfigValueManager.mockConfiguration("MovieManager.minimumSizeFor1080", 5000);
		ConfigValueManager.mockConfiguration("MovieManager.minimumSizeFor1080", 3000);
	}

	@Test
	public void testFindDownloadForMovie() throws Exception {
		KATProvider finder = new KATProvider();
		List<SearchResult> results = finder.findMovie("Robocop", 2013, VideoQuality._1080p, Language.EN, Language.FR);
		for (SearchResult searchResult: results) {
			System.out.println( searchResult );
		}
		
	}

}
