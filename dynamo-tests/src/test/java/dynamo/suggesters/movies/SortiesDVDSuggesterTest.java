package dynamo.suggesters.movies;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class SortiesDVDSuggesterTest extends AbstractDynamoTest {

	@Test
	public void testSuggestMovies() throws Exception {
		new SortiesDVDSuggester().suggestMovies();
	}

}
