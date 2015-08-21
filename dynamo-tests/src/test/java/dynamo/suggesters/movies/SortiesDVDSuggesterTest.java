package dynamo.suggesters.movies;

import org.junit.Test;

public class SortiesDVDSuggesterTest {

	@Test
	public void testSuggestMovies() throws Exception {
		new SortiesDVDSuggester().suggestMovies();
	}

}
