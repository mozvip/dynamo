package dynamo.providers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.model.result.SearchResult;
import dynamo.tests.AbstractDynamoTest;

public class UsenetCrawlerProviderTest extends AbstractDynamoTest {
	
	private static UsenetCrawlerProvider provider;
	
	@BeforeClass
	public static void setup() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		ConfigurationManager.mockConfiguration("UsenetCrawlerProvider.login", privateData.getString("UsenetCrawlerProvider.login"));
		ConfigurationManager.mockConfiguration("UsenetCrawlerProvider.password", privateData.getString("UsenetCrawlerProvider.password"));
		ConfigurationManager.mockConfiguration("UsenetCrawlerProvider.enabled", true);
		provider = DynamoObjectFactory.getInstance(UsenetCrawlerProvider.class);
	}

	@Test
	public void testFindMovie() throws Exception {
		List<SearchResult> results = provider.findMovie("Mad Max : Fury Road", 2015, VideoQuality._1080p, Language.EN, Language.FR);
		for (SearchResult searchResult : results) {
			provider.download( searchResult.getUrl(), searchResult.getReferer() );
		}
	}

}
