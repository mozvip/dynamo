package dynamo.providers.magazines;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class EBookWTest extends AbstractDynamoTest {

	@Test
	public void testSuggestIssues() throws Exception {
		new EBookW().suggestIssues();
	}

}
