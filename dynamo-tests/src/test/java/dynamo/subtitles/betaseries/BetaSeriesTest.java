package dynamo.subtitles.betaseries;

import org.junit.BeforeClass;

import dynamo.core.SubtitlesFinder;
import dynamo.core.manager.ConfigValueManager;
import dynamo.subtitles.AbstractSubtitleFinderTestCase;


public class BetaSeriesTest extends AbstractSubtitleFinderTestCase {

	@BeforeClass
	public static void config() {
		ConfigValueManager.mockConfiguration("BetaSeries.enabled", "true");
		ConfigValueManager.mockConfiguration("BetaSeries.login", privateData.getString("BetaSeries.login"));
		ConfigValueManager.mockConfiguration("BetaSeries.password", privateData.getString("BetaSeries.password"));
	}

	@Override
	public Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass() {
		return BetaSeries.class;
	}

}
