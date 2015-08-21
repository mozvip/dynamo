package dynamo.model.tvshows;

import org.junit.BeforeClass;
import org.junit.Test;

import com.omertron.thetvdbapi.model.Series;

import dynamo.core.Language;

public class TVShowManagerTest {
	
	@BeforeClass
	public static void init() {
		TVShowManager.getInstance().setEnabled( true );
		TVShowManager.getInstance().setMetaDataLanguage( Language.EN );
		TVShowManager.getInstance().reconfigure();
	}

	@Test
	public void getAwkwardTest() {
		Series series = TVShowManager.getInstance().searchTVShow("Awkward");
		
		org.junit.Assert.assertNotNull( series );
	}

}
