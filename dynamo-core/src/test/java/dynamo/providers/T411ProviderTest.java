package dynamo.providers;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.manager.ConfigAnnotationManager;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.model.result.SearchResult;
import dynamo.tests.AbstractDynamoTest;

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
		assert( results.size() > 0);
	}

}
