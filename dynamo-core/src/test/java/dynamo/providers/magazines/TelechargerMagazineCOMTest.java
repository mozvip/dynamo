package dynamo.providers.magazines;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.magazines.KioskIssuesSuggesterException;

public class TelechargerMagazineCOMTest {
	
	private static TelechargerMagazineCOM suggester;
	
	@BeforeClass
	public static void init() {
		suggester = new TelechargerMagazineCOM();
	}

	@Test
	public void testSuggestIssues() throws KioskIssuesSuggesterException {
		suggester.suggestIssues();
	}

}
