package dynamo.subtitles.tvsubtitles.net;

import dynamo.core.SubtitlesFinder;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;


public class TVSubtitlesNetTest extends AbstractSubtitleFinderTestCase {
	
	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return TVSubtitlesNet.class;
	}

}
