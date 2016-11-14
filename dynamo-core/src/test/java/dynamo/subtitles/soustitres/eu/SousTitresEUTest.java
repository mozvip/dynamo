package dynamo.subtitles.soustitres.eu;

import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.VideoDetails;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.tests.AbstractDynamoTest;

public class SousTitresEUTest extends AbstractDynamoTest {

	@Test
	public void test() throws Exception {
		SousTitresEU finder = new SousTitresEU();
		finder.reconfigure();
		
		VideoDetails details = new VideoDetails(null, "Insecure", VideoQuality._720p, VideoSource.HDTV, "SVA", 1, 5, null);
		RemoteSubTitles findSubtitles = finder.findSubtitles(details, Language.FR); 
	}

}
