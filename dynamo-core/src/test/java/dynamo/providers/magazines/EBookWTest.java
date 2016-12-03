package dynamo.providers.magazines;

import org.junit.Test;

import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.tests.AbstractDynamoTest;

public class EBookWTest extends AbstractDynamoTest {

	@Test
	public void testSuggestIssues() throws KioskIssuesSuggesterException {
		new EBookW().extractFromPage(0);
	}

}
