package dynamo.providers;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import dynamo.core.Language;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;


public class CPasBienFinderTest extends AbstractProviderTest {
	
	CPasBienProvider finder = new CPasBienProvider();

	@Test
	public void testFindDownloadsForEpisode() {
		try {
			List<SearchResult> results = finder.findDownloadsForEpisode("Game of Thrones", createMockedSeries("Game of Thrones", Language.EN), 3, 4);
			for (SearchResult searchResult : results) {
				System.out.println( searchResult );
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testFindMusicAlbum() {
		try {
			List<SearchResult> results = finder.findMusicAlbum( "Madonna", "MDNA", MusicQuality.COMPRESSED );
			for (SearchResult searchResult : results) {
				System.out.println( searchResult );
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
