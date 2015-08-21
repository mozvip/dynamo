package dynamo.magazines;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.junit.Test;

import dynamo.core.manager.DynamoObjectFactory;
import dynamo.tests.AbstractDynamoTest;

public class MagazinesSuggesterTest extends AbstractDynamoTest {
	
	@Test
	public void test() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, KioskIssuesSuggesterException {
		
		DynamoObjectFactory<KioskIssuesSuggester> df = new DynamoObjectFactory<>("dynamo", KioskIssuesSuggester.class);
		Set<KioskIssuesSuggester> suggesters = df.getInstances();
		for (KioskIssuesSuggester kioskIssuesSuggester : suggesters) {
			kioskIssuesSuggester.suggestIssues();
		}
		
	}

}
