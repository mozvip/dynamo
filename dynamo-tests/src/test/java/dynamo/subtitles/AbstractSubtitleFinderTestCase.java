package dynamo.subtitles;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.tests.AbstractDynamoTest;

public abstract class AbstractSubtitleFinderTestCase extends AbstractDynamoTest {

	private SubtitlesFinder webSite;

	public abstract Class<? extends SubtitlesFinder> getTVShowsSubsWebSiteClass();
	
	public void mockSpecificConfig() {}
	
	@Before
	public void initInstance() throws Exception {
		ConfigValueManager.mockConfiguration( getTVShowsSubsWebSiteClass().getSimpleName() + ".enabled", Boolean.TRUE);
		ConfigValueManager.mockConfiguration( "SubTitleDownloader.enabled", Boolean.TRUE);
		mockSpecificConfig();
		webSite = DynamoObjectFactory.getInstanceAndConfigure( getTVShowsSubsWebSiteClass() );
	}
	
	@Test
	public void testMrRobot() throws Exception {
		VideoDetails details = new VideoDetails( null, "Mr. Robot",VideoQuality._720p, VideoSource.HDTV, null, 1, 10, null );
		
		RemoteSubTitles subTitles = webSite.findSubtitles( details, Language.FR );
		Assert.assertNotNull( subTitles );
	}

	@Test
	public void testGOTS03E04_French() throws Exception {
		VideoDetails details = new VideoDetails( null, "Game of Thrones",VideoQuality._720p, VideoSource.HDTV, "EVOLVE", 3, 4, null );
		
		RemoteSubTitles subTitles = webSite.findSubtitles( details, Language.FR );
		Assert.assertNotNull( subTitles );
	}
	
	@Test
	public void testRaisingHopeS03E18_French() throws Exception {
		VideoDetails details = new VideoDetails( null, "Raising Hope",VideoQuality._720p, VideoSource.HDTV, "DIMENSION", 3, 18, null );
		
		RemoteSubTitles subTitles = webSite.findSubtitles( details, Language.FR );
		Assert.assertNotNull( subTitles );
	}

	@Test
	public void testTheKnickS01E06_French() throws Exception {
		VideoDetails details = new VideoDetails( null, "The Knick",VideoQuality._720p, VideoSource.HDTV, "DIMENSION", 1, 6, null );
		
		RemoteSubTitles subTitles = webSite.findSubtitles( details, Language.FR );
		Assert.assertNotNull( subTitles );
	}

	@Test
	public void testAmericanHorrorStoryS01E01_French() throws Exception {
		VideoDetails details = new VideoDetails( null, "American Horror Story",VideoQuality._720p, VideoSource.WEB_DL, "CTRLHD", 1, 1, null );
		
		RemoteSubTitles subTitles = webSite.findSubtitles( details, Language.FR );
		Assert.assertNotNull( subTitles );
	}
	
	@Test
	public void testTrueBloodS07E04_English() throws Exception {
		VideoDetails details = new VideoDetails( null, "True Blood",VideoQuality._720p, VideoSource.WEB_DL, "KILLERS", 7, 4, null );
		
		RemoteSubTitles subTitles = webSite.findSubtitles( details, Language.EN );
		Assert.assertNotNull( subTitles );
	}

}
