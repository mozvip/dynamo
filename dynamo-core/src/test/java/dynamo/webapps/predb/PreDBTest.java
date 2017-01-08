package dynamo.webapps.predb;

import org.junit.Test;

import com.github.dynamo.tests.AbstractDynamoTest;
import com.github.dynamo.webapps.predb.PreDB;

public class PreDBTest extends AbstractDynamoTest {
	
	private PreDB predb = PreDB.getInstance();

	@Test
	public void testSuggestMovies() throws Exception {
		predb.suggestMovies();
	}

}
