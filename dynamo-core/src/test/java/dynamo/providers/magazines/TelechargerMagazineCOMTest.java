package dynamo.providers.magazines;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dynamo.magazines.KioskIssuesSuggesterException;
import com.github.dynamo.providers.magazines.TelechargerMagazineCOM;

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
