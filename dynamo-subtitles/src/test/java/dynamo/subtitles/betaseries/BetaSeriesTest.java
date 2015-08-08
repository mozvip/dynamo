package dynamo.subtitles.betaseries;

import org.junit.BeforeClass;

import dynamo.core.SubtitlesFinder;
import dynamo.core.manager.ConfigurationManager;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;


public class BetaSeriesTest extends AbstractSubtitleFinderTestCase {

	@BeforeClass
	public static void config() {
		ConfigurationManager.mockConfiguration("BetaSeries.enabled", "true");
		ConfigurationManager.mockConfiguration("BetaSeries.login", privateData.getString("BetaSeries.login"));
		ConfigurationManager.mockConfiguration("BetaSeries.password", privateData.getString("BetaSeries.password"));
	}

	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return BetaSeries.class;
	}

}
