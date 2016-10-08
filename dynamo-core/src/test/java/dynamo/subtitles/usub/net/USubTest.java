package dynamo.subtitles.usub.net;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.VideoDetails;
import dynamo.core.VideoQuality;
import dynamo.tests.AbstractDynamoTest;

public class USubTest extends AbstractDynamoTest {
	
	private static USub usub ;
	
	@BeforeClass
	public static void init() {
		usub = new USub();
		usub.reconfigure();
	}
	

	@Test
	public void testDownloadSubtitle() throws Exception {
		VideoDetails details = new VideoDetails(null, "Scream Queens (2015)", VideoQuality._1080p, null, null, 2, 1, null);
		usub.downloadSubtitle(details, Language.FR);
	}

}
