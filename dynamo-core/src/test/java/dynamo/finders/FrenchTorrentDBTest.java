package dynamo.finders;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.model.music.MusicQuality;
import dynamo.providers.FrenchTorrentDBProvider;
import dynamo.tests.AbstractDynamoTest;
import junit.framework.Assert;

public class FrenchTorrentDBTest extends AbstractDynamoTest {

	private static FrenchTorrentDBProvider provider = new FrenchTorrentDBProvider();

	@BeforeClass
	public static void initClass() throws Exception {

		provider.setEnabled( true );
		provider.setLogin( privateData.getString("frenchtorrentdb.login"));
		provider.setPassword( privateData.getString("frenchtorrentdb.password"));

		provider.configureProvider();
		Assert.assertTrue(provider.isEnabled());
	}

	@Test
	public void test() throws Exception {
		Assert.assertTrue( provider.findMusicAlbum("Maroon 5", "V", MusicQuality.COMPRESSED).size() > 0);
		Assert.assertTrue( provider.findMovie("Une Rencontre", 2014, VideoQuality._1080p, Language.EN, Language.FR).size() > 0);
	}

}
