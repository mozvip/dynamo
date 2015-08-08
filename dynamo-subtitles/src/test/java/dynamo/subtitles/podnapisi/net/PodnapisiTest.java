package dynamo.subtitles.podnapisi.net;

import dynamo.core.SubtitlesFinder;
import dynamo.core.manager.ConfigurationManager;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;


public class PodnapisiTest extends AbstractSubtitleFinderTestCase {
	
	@Override
	public void mockSpecificConfig() {
		ConfigurationManager.mockConfiguration("Podnapisi.login", privateData.getString("Podnapisi.login"));
		ConfigurationManager.mockConfiguration("Podnapisi.password", privateData.getString("Podnapisi.password"));
	}
	
	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return Podnapisi.class;
	}

}
