package dynamo.subtitles.addic7ed;

import dynamo.core.SubtitlesFinder;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;

public class Addic7edTest extends AbstractSubtitleFinderTestCase {
	
	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return Addic7ed.class;
	}

}
