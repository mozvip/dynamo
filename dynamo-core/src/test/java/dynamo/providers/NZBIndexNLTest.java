package dynamo.providers;

import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.VideoQuality;

public class NZBIndexNLTest {
	
	@Test
	public void testFindMovie() throws Exception {
		NZBIndexNLProvider test = new NZBIndexNLProvider();
		test.configureProvider();
		test.findMovie("Pi", 2012, VideoQuality._1080p, Language.EN, Language.FR);
	}

}
