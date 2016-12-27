package dynamo.webapps.predb;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class PreDBTest extends AbstractDynamoTest {
	
	private PreDB predb = PreDB.getInstance();

	@Test
	public void testSuggestMovies() throws Exception {
		predb.suggestMovies();
	}

}
