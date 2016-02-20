package dynamo.magazines;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import dynamo.core.manager.DynamoObjectFactory;
import dynamo.tests.AbstractDynamoTest;

public class MagazinesSuggesterTest extends AbstractDynamoTest {
	
	@Test
	public void test() throws Exception {
		
		List<Path> folders = new ArrayList<>();
		folders.add( Paths.get(this.getClass().getClassLoader().getResource("magazines").toURI()));
		MagazineManager.getInstance().setFolders(folders);
		
		DynamoObjectFactory<KioskIssuesSuggester> df = new DynamoObjectFactory<>("dynamo", KioskIssuesSuggester.class);
		Set<KioskIssuesSuggester> suggesters = df.getInstances();
		for (KioskIssuesSuggester kioskIssuesSuggester : suggesters) {
			kioskIssuesSuggester.suggestIssues();
		}
		
	}

}
