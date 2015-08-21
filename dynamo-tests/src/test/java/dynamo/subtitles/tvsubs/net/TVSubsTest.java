package dynamo.subtitles.tvsubs.net;

import dynamo.core.SubtitlesFinder;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;

public class TVSubsTest extends AbstractSubtitleFinderTestCase {
	
	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return TVSubs.class;
	}

}
