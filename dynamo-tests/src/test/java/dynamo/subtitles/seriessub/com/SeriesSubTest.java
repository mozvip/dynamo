package dynamo.subtitles.seriessub.com;

import dynamo.core.SubtitlesFinder;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;


public class SeriesSubTest extends AbstractSubtitleFinderTestCase {
	
	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return SeriesSub.class;
	}

}
