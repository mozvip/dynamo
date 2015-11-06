package dynamo.providers;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import junit.framework.Assert;


public class T411ProviderTest extends AbstractProviderTest {
	
	static T411Provider finder;
	
	@BeforeClass
	public static void initTest() throws Exception {
		ConfigValueManager.mockConfiguration("T411Provider.baseURL", "http://www.t411.in");
		ConfigValueManager.mockConfiguration("T411Provider.enabled", true);
		finder = (T411Provider) DynamoObjectFactory.getInstanceAndConfigure( T411Provider.class );
	}

	@Test
	public void testFindDownloadsForEpisode() throws Exception {
		List<SearchResult> results = finder.findDownloadsForEpisode("Game of Thrones", Language.EN, 3, 4);
		Assert.assertNotNull( results );
		for (SearchResult searchResult : results) {
			System.out.println( searchResult );
		}
	}
	
	@Test
	public void testFindDownloadsForSeason() throws Exception {
		List<SearchResult> results = finder.findDownloadsForSeason("Awkward", Language.EN, 2);
		Assert.assertNotNull( results );
		for (SearchResult searchResult : results) {
			System.out.println( searchResult );
		}		
	}

	@Test
	public void testFindMusicAlbum() throws Exception {
		List<SearchResult> results = finder.findMusicAlbum("Madonna", "MDNA", MusicQuality.LOSSLESS);
		Assert.assertNotNull( results );
		for (SearchResult searchResult : results) {
			System.out.println( searchResult );
		}		
	}
	
	@Test
	public void testSuggestMovies() throws Exception {
		finder.suggestMovies();
	}

}
