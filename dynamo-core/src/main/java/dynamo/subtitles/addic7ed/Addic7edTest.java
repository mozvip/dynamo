package dynamo.subtitles.addic7ed;

import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.VideoDetails;
import dynamo.tests.AbstractDynamoTest;

public class Addic7edTest extends AbstractDynamoTest {

	@Test
	public void testFindSubtitles() throws Exception {

		Addic7ed finder = new Addic7ed();
		finder.reconfigure();
		
		VideoDetails details = new VideoDetails(null, "American Crime", null, null, null, 1, 1, null);
		
		RemoteSubTitles findSubtitles = finder.findSubtitles(details, Language.FR);
		
	}

}
