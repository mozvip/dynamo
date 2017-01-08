package dynamo.providers;

import org.junit.Test;

import com.github.dynamo.providers.RARBGProvider;
import com.github.dynamo.tests.AbstractDynamoTest;

public class RARBGProviderTest extends AbstractDynamoTest {

	@Test
	public void testSuggestMovies() throws Exception {
		RARBGProvider provider = new RARBGProvider();
		provider.suggestMovies();
	}

}
