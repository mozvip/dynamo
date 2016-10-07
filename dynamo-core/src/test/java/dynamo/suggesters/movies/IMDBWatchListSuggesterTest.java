package dynamo.suggesters.movies;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class IMDBWatchListSuggesterTest extends AbstractDynamoTest {

	@Test
	public void testExtractIMDBTitle() throws IOException {
		IMDBTitle extractIMDBTitle = IMDBWatchListSuggester.extractIMDBTitle("tt2111478");
		assertTrue( extractIMDBTitle.getGenres().contains("Documentary") );
	}

}
