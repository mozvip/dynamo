package dynamo.providers.magazines;

import org.junit.Test;

import dynamo.magazines.KioskIssuesSuggesterException;

public class RLSBBComTest {

	@Test
	public void test() throws KioskIssuesSuggesterException {
		new RLSBBCom().extractFromPage(1);
	}

}
