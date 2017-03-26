package dynamo.providers;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.providers.RARBGProvider;
import com.github.dynamo.tests.AbstractDynamoTest;

public class RARBGProviderTest extends AbstractDynamoTest {

	@Test
	public void testSuggestMovies() throws Exception {
		RARBGProvider provider = new RARBGProvider();
		provider.suggestMovies();
	}
	
	@Test
	public void testFindMovie() throws Exception {
		RARBGProvider provider = new RARBGProvider();
		List<SearchResult> results = provider.findMovie("Rogue One: A Star Wars Story", 2016, VideoQuality._1080p, Language.EN, Language.FR);
		Assert.assertTrue( results.size() > 0);
		for (SearchResult searchResult : results) {

		}
	}

}
