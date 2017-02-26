package dynamo.providers;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.ConfigAnnotationManager;
import com.github.dynamo.core.manager.ConfigurationManager;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.providers.T411Provider;
import com.github.dynamo.tests.AbstractDynamoTest;

public class T411ProviderTest extends AbstractDynamoTest {
	
	static T411Provider provider;
	
	@BeforeClass
	public static void beforeTest() {
		ConfigAnnotationManager.mockConfiguration("T411Provider.enabled", Boolean.TRUE);
	
		provider = DynamoObjectFactory.getInstance( T411Provider.class );
		ConfigurationManager.getInstance().configureInstance( provider );
	}

	@Test
	public void testFindEpisodeStringLanguageIntInt() throws Exception {
		List<SearchResult> results = provider.findEpisode("The Exorcist", Language.EN, 1, 1);
		for (SearchResult searchResult : results) {
			assert( searchResult.getTitle().contains("S01E01"));
		}		
		assert( results.size() > 0);
	}

	@Test
	public void testFindEpisode() throws Exception {
		List<SearchResult> results = provider.findEpisode("Walking Dead", Language.EN, 7, 10);
		for (SearchResult searchResult : results) {
			assert( searchResult.getTitle().contains("S07E10"));
		}
		assert( results.size() > 0);
	}
}
