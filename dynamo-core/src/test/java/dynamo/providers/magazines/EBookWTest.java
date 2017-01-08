package dynamo.providers.magazines;

import org.junit.Test;

import com.github.dynamo.magazines.KioskIssuesSuggesterException;
import com.github.dynamo.providers.magazines.EBookW;
import com.github.dynamo.tests.AbstractDynamoTest;

public class EBookWTest extends AbstractDynamoTest {

	@Test
	public void testSuggestIssues() throws KioskIssuesSuggesterException {
		new EBookW().extractFromPage(0);
	}

}
