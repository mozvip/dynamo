package dynamo.providers;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.model.result.SearchResult;

public class EZTVProviderTest extends AbstractProviderTest {
	
	static EZTVProvider provider;
	
	@BeforeClass
	public static void initTest() throws Exception {
		ConfigValueManager.mockConfiguration("EZTVProvider.enabled", true);
		ConfigValueManager.mockConfiguration("EZTVProvider.baseURL", "https://eztv.ag");		
		provider = (EZTVProvider) DynamoObjectFactory.getInstanceAndConfigure( EZTVProvider.class );
	}
	

	@Test
	public void test1() throws Exception {
		List<SearchResult> results = provider.findDownloadsForEpisode("Mr Robot", Language.EN, 1, 5);
		Assert.assertTrue( results != null && results.size() > 0);
	}

	@Test
	public void test2() throws Exception {
		List<SearchResult> results = provider.findDownloadsForEpisode("Game of Thrones", Language.EN, 1, 5);
		Assert.assertTrue( results != null && results.size() > 0);
	}

	@Test
	public void test3() throws Exception {
		List<SearchResult> results = provider.findDownloadsForEpisode("Dexter", Language.EN, 7, 5);
		Assert.assertTrue( results != null && results.size() > 0);
	}

	@Test
	public void test4() throws Exception {
		List<SearchResult> results = provider.findDownloadsForEpisode("Peaky Blinders", Language.EN, 3, 3);
		Assert.assertTrue( results != null && results.size() > 0);
	}

}
