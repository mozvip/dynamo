package dynamo.subtitles.opensubtitles;

import dynamo.core.SubtitlesFinder;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;

public class OpenSubtitlesOrgTest extends AbstractSubtitleFinderTestCase {
	
	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return OpenSubtitlesOrg.class;
	}

}
