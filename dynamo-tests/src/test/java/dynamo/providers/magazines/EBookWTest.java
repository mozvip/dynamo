package dynamo.providers.magazines;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.magazines.MagazineManager;
import dynamo.tests.AbstractDynamoTest;

public class EBookWTest extends AbstractDynamoTest {
	
	@BeforeClass
	public static void initTest() {
		List<Path> folders = new ArrayList<>();
		folders.add( Paths.get("."));
		MagazineManager.getInstance().setFolders(folders);		
	}

	@Test
	public void testSuggestIssues() throws Exception {
		new EBookW().suggestIssues();
	}

}
